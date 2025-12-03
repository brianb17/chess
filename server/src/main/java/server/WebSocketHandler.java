package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.JoinGameRequest;
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
    private static final Map<String, WsConnectContext> sessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    private static final Map<String, Integer> sessionToGame = new ConcurrentHashMap<>();

    public UserService userService;
    public GameService gameService;

    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void onConnect(WsConnectContext ctx) {
        sessions.put(ctx.sessionId(), ctx);
        System.out.println("WebSocket connected " + ctx.sessionId());
    }

    public void onMessage(WsMessageContext ctx) {
        String msg = ctx.message();
        System.out.println("Websocket message received: " + msg);

        try {
            JsonObject json = gson.fromJson(msg, JsonObject.class);
            String commandType = json.get("commandType").getAsString();

            switch (commandType) {
                case "CONNECT" -> handleConnect(ctx, json);
                case "MAKE_MOVE" -> {
                    MakeMoveCommand cmd = gson.fromJson(msg, MakeMoveCommand.class);
                    handleMakeMove(ctx, cmd);
                }
                case "RESIGN" -> {
                    UserGameCommand cmd = gson.fromJson(msg, UserGameCommand.class);
                    handleResign(ctx, cmd);
                }
                case "LEAVE" -> {
                    UserGameCommand cmd = gson.fromJson(msg, UserGameCommand.class);
                    handleLeave(ctx, cmd);
                    break;
                }
                default -> sendError(ctx, "Unknown commandType: " + commandType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        Integer leavingGameID = sessionToGame.get(ctx.sessionId());
        sessionToGame.remove(ctx.sessionId());

        for (WsConnectContext otherCtx : sessions.values()) {
            Integer otherGameID = sessionToGame.get(otherCtx.sessionId());
            if (!otherCtx.sessionId().equals(ctx.sessionId()) &&
                    leavingGameID != null &&
                    leavingGameID.equals(otherGameID)) {
                otherCtx.send(gson.toJson(notif));
            }
        }

        sessions.remove(ctx.sessionId());
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

        for (WsConnectContext sessionCtx : sessions.values()) {
            Integer otherGameID = sessionToGame.get(sessionCtx.sessionId());
            if (otherGameID != null && otherGameID.equals(gameID)) {
                sessionCtx.send(gson.toJson(notif));
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

            String color = null;
            if (game.whiteUsername() == null) {
                color = "WHITE";
            } else if (game.blackUsername() == null) {
                color = "BLACK";
            }

            if (color != null) {
                try {
                    JoinGameRequest request = new JoinGameRequest(gameID, color);
                    gameService.joinGame(authToken, request);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Join failed: " + e.getMessage());
                    sendError(ctx, "Error: " + e.getMessage());
                    return;
                }
            }

            // Send the updated game to the connecting user
            GameData updatedGame = gameService.getGameById(gameID);
            JsonObject loadGame = new JsonObject();
            loadGame.addProperty("serverMessageType", "LOAD_GAME");
            loadGame.add("game", gson.toJsonTree(updatedGame));
            ctx.send(gson.toJson(loadGame));

            sessionToGame.put(ctx.sessionId(), gameID);

            // Notify other sessions
            for (WsConnectContext otherCtx : sessions.values()) {
                if (!otherCtx.sessionId().equals(ctx.sessionId()) &&
                        gameID == sessionToGame.get(otherCtx.sessionId())) { // only same game
                    JsonObject notif = new JsonObject();
                    notif.addProperty("serverMessageType", "NOTIFICATION");

                    String joiningAs = (color == null) ? "SPECTATOR" : color;
                    notif.addProperty("message", username + " joined the game as " + joiningAs);
                    otherCtx.send(gson.toJson(notif));
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
            loadGame.add("game", gson.toJsonTree(updatedGameData));

            JsonObject notif = new JsonObject();
            notif.addProperty("serverMessageType", "NOTIFICATION");
            notif.addProperty("message", username + " made a move.");

            for (WsConnectContext sessionCtx : sessions.values()) {
                Integer otherGameID = sessionToGame.get(sessionCtx.sessionId());
                if (otherGameID != null && otherGameID.equals(gameID)) {
                    sessionCtx.send(gson.toJson(loadGame));
                    if (!sessionCtx.sessionId().equals(ctx.sessionId())) {
                        sessionCtx.send(gson.toJson(notif));
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
        ctx.send(gson.toJson(error));
    }

    public void onClose(WsCloseContext ctx) {
        String sessionId = ctx.sessionId();
        sessions.remove(sessionId);
        sessionToGame.remove(sessionId);
        System.out.println("WebSocket closed: " + sessionId);
    }

}
