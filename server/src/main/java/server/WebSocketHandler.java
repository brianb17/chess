package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import datamodel.GameData;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private static final Map<String, WsConnectContext> SESSIONS = new ConcurrentHashMap<>();
    private static final Gson GSON = new Gson();
    private static final Map<String, Integer> SESSION_TO_GAME = new ConcurrentHashMap<>();

    public UserService userService;
    public GameService gameService;

    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void onConnect(WsConnectContext ctx) {
        SESSIONS.put(ctx.sessionId(), ctx);
        System.out.println("WebSocket connected " + ctx.sessionId());
    }


    public void onMessage(WsMessageContext ctx) {
        String msg = ctx.message();
        System.out.println("Websocket message received: " + msg);

        try {
            JsonObject json = GSON.fromJson(msg, JsonObject.class);
            String commandType = json.get("commandType").getAsString();

            switch (commandType) {
                case "CONNECT" -> handleConnect(ctx, json);
                case "MAKE_MOVE" -> {
                    MakeMoveCommand cmd = GSON.fromJson(msg, MakeMoveCommand.class);
                    handleMakeMove(ctx, cmd);
                }
                case "RESIGN" -> {
                    UserGameCommand cmd = GSON.fromJson(msg, UserGameCommand.class);
                    handleResign(ctx, cmd);
                }
                case "LEAVE" -> {
                    UserGameCommand cmd = GSON.fromJson(msg, UserGameCommand.class);
                    handleLeave(ctx, cmd);
                    break;
                }
                default -> sendError(ctx, "Unknown commandType: " + commandType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPositionDescription(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        int row = pos.getRow();
        return String.valueOf(col) + row;
    }

    private String getMoveDescription(ChessMove move) {
        String start = getPositionDescription(move.getStartPosition());
        String end = getPositionDescription(move.getEndPosition());

        if (move.getPromotionPiece() != null) {
            return String.format("%s to %s (Promotes to %s)",
                    start,
                    end,
                    move.getPromotionPiece().toString());
        }
        return start + " to " + end;
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand cmd) {
        String authToken = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();
        String username = userService.getUsernameFromToken(authToken);

        if (username == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        try {
            GameData gameData = gameService.getGameById(gameID);
            if (gameData != null) {
                String white = gameData.whiteUsername();
                String black = gameData.blackUsername();

                if (username.equals(white)) {
                    white = null;
                } else if (username.equals(black)) {
                    black = null;
                }

                ChessGame chess = gameData.game();
                GameData updated = new GameData(
                        gameData.gameID(),
                        white,
                        black,
                        gameData.gameName(),
                        chess
                );
                gameService.updateGame(updated);
            }
        } catch (Exception ignored) {
        }

        JsonObject notif = new JsonObject();
        notif.addProperty("serverMessageType", "NOTIFICATION");
        notif.addProperty("message", username + " left the game");

        Integer leavingGameID = SESSION_TO_GAME.get(ctx.sessionId());
        SESSION_TO_GAME.remove(ctx.sessionId());

        for (WsConnectContext otherCtx : SESSIONS.values()) {
            Integer otherGameID = SESSION_TO_GAME.get(otherCtx.sessionId());
            if (!otherCtx.sessionId().equals(ctx.sessionId()) &&
                    leavingGameID != null &&
                    leavingGameID.equals(otherGameID)) {
                otherCtx.send(GSON.toJson(notif));
            }
        }

        SESSIONS.remove(ctx.sessionId());
        try {
            ctx.closeSession();
        } catch (Exception ignored) {

        }
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand cmd) throws DataAccessException {
        String authToken = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();
        String username = userService.getUsernameFromToken(authToken);

        if (username == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = gameService.getGameById(gameID);
        if (gameData == null) {
            sendError(ctx, "Error: invalid gameID");
            return;
        }

        if (gameData.game().isGameOver()) {
            sendError(ctx, "Game is already over.");
            return;
        }

        String winner;
        if (username.equals(gameData.whiteUsername())) {
            winner = gameData.blackUsername();
        } else if (username.equals(gameData.blackUsername())) {
            winner = gameData.whiteUsername();
        } else {
            sendError(ctx, "Error: you are not a player in this game");
            return;
        }
        ChessGame chess = gameData.game();
        chess.setGameOver(true);

        GameData updatedGame = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                chess
        );
        gameService.updateGame(updatedGame);

        JsonObject notif = new JsonObject();
        notif.addProperty("serverMessageType", "NOTIFICATION");
        notif.addProperty("message", username + " resigned. " + winner + " wins!");

        for (WsConnectContext sessionCtx : SESSIONS.values()) {
            Integer otherGameID = SESSION_TO_GAME.get(sessionCtx.sessionId());
            if (otherGameID != null && otherGameID.equals(gameID)) {
                sessionCtx.send(GSON.toJson(notif));
            }
        }
    }

    private void handleConnect(WsMessageContext ctx, JsonObject json) {
        try {
            String authToken = json.get("authToken").getAsString();
            int gameID = json.get("gameID").getAsInt();
            String username = userService.getUsernameFromToken(authToken);

            if (username == null) {
                System.out.println("Bad AuthToken: " + authToken);
                sendError(ctx, "Error: unauthorized");
                return;
            }

            GameData game = gameService.getGameById(gameID);
            if (game == null) {
                System.out.println("Game not found: " + gameID);
                sendError(ctx, "Error: invalid gameID");
                return;
            }

            // No auto-join here. Determine what role this connection already has (if any)
            String joiningAs;
            if (username.equals(game.whiteUsername())) {
                joiningAs = "WHITE";
            } else if (username.equals(game.blackUsername())) {
                joiningAs = "BLACK";
            } else {
                joiningAs = "SPECTATOR";
            }

            // Send the current game state to the connecting user
            GameData updatedGame = gameService.getGameById(gameID);
            JsonObject loadGame = new JsonObject();
            loadGame.addProperty("serverMessageType", "LOAD_GAME");
            loadGame.add("game", GSON.toJsonTree(updatedGame));
            ctx.send(GSON.toJson(loadGame));

            // Track which game this websocket session is watching
            SESSION_TO_GAME.put(ctx.sessionId(), gameID);

            // Notify other sessions in the same game that someone joined (as spectator or as a player if they were already assigned)
            for (WsConnectContext otherCtx : SESSIONS.values()) {
                if (!otherCtx.sessionId().equals(ctx.sessionId())) {
                    Integer otherGameID = SESSION_TO_GAME.get(otherCtx.sessionId());
                    if (otherGameID != null && otherGameID.equals(gameID)) {
                        JsonObject notif = new JsonObject();
                        notif.addProperty("serverMessageType", "NOTIFICATION");
                        notif.addProperty("message", username + " joined the game as " + joiningAs);
                        otherCtx.send(GSON.toJson(notif));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ctx, "Error processing CONNECT command: " + e.getMessage());
        }
    }




    private void handleMakeMove(WsMessageContext ctx, MakeMoveCommand cmd) {
        String authToken = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();
        ChessMove move = cmd.getMove();

        String username = userService.getUsernameFromToken(authToken);
        if (username == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = gameService.getGameById(gameID);
        if (gameData == null) {
            sendError(ctx, "Error: invalid gameID");
            return;
        }

        ChessGame chess = gameData.game();
        if (chess.isGameOver()) {
            sendError(ctx, "Game is over, no more moves allowed.");
            return;
        }

        try {
            ChessGame.TeamColor playerColorEnum;
            if (username.equals(gameData.whiteUsername())) {
                playerColorEnum = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())) {
                playerColorEnum = ChessGame.TeamColor.BLACK;
            } else {
                sendError(ctx, "Invalid move: You are not a player in this game");
                return;
            }

            if (chess.getTeamTurn() != playerColorEnum) {
                sendError(ctx, "Invalid move: Not your turn");
                return;
            }
            chess.makeMove(move);
            String moveDescription = getMoveDescription(move);

            GameData updatedGameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    chess
            );
            gameService.updateGame(updatedGameData);

            JsonObject loadGame = new JsonObject();
            loadGame.addProperty("serverMessageType", "LOAD_GAME");
            loadGame.add("game", GSON.toJsonTree(updatedGameData.game()));

            String baseMessage = String.format("%s played %s", username, moveDescription);
            String messageSuffix = "";
            ChessGame.TeamColor oppColor = chess.getTeamTurn();

            String oppUsername = (oppColor == ChessGame.TeamColor.WHITE) ?
                                updatedGameData.whiteUsername() :
                                updatedGameData.blackUsername();

            if (chess.isInCheckmate(oppColor)) {
                messageSuffix = String.format(" Game over! %s is in checkmate. %s wins!",
                        oppUsername,
                        username);
                chess.setGameOver(true); // Ensure the game object reflects game over
                gameService.updateGame(updatedGameData); // Re-save the game state (for isGameOver)

            } else if (chess.isInCheck(oppColor)) {
                messageSuffix = String.format(" %s is in check!", oppUsername);

            } else if (chess.isInStalemate(oppColor)) {
                messageSuffix = " Game over! It's a stalemate.";
                chess.setGameOver(true);
                gameService.updateGame(updatedGameData);
            }

            JsonObject notif = new JsonObject();
            notif.addProperty("serverMessageType", "NOTIFICATION");
            notif.addProperty("message", baseMessage + messageSuffix);

            for (WsConnectContext sessionCtx : SESSIONS.values()) {
                Integer otherGameID = SESSION_TO_GAME.get(sessionCtx.sessionId());
                if (otherGameID != null && otherGameID.equals(gameID)) {
                    sessionCtx.send(GSON.toJson(loadGame));
                    if (!sessionCtx.sessionId().equals(ctx.sessionId())) {
                        sessionCtx.send(GSON.toJson(notif));
                    }
                }
            }


        } catch (InvalidMoveException e) {
            sendError(ctx, "Invalid move: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendError(WsMessageContext ctx, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("serverMessageType", "ERROR");
        error.addProperty("errorMessage", message);
        ctx.send(GSON.toJson(error));
    }

    public void onClose(WsCloseContext ctx) {
        String sessionId = ctx.sessionId();
        SESSIONS.remove(sessionId);
        SESSION_TO_GAME.remove(sessionId);
        System.out.println("WebSocket closed: " + sessionId);
    }

}
