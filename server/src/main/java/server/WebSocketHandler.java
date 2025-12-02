package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import datamodel.GameData;
import datamodel.JoinGameRequest;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private static final Map<String, WsConnectContext> sessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
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

            if ("CONNECT".equals(commandType)) {
                String authToken = json.get("authToken").getAsString();
                int gameID = json.get("gameID").getAsInt();
                String username = userService.getUsernameFromToken(authToken);
                if (username == null) {
                    System.out.println("Invalid auth token: " + authToken);
                    return;
                }

                GameData game = gameService.getGameById(gameID);
                if (game == null) {
                    System.out.println("Game not found: " + gameID);
                    return;
                }

                String color;
                if (game.whiteUsername() == null) color = "WHITE";
                else if (game.blackUsername() == null) color = "BLACK";
                else color = null; // both spots taken

                if (color != null) {
                    JoinGameRequest request = new JoinGameRequest(gameID, color);
                    try {
                        gameService.joinGame(authToken, request);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        System.out.println("Join failed: " + e.getMessage());
                        return;
                    }
                }

                GameData updatedGame = gameService.getGameById(gameID);
                JsonObject gameJson = new JsonObject();
                gameJson.addProperty("gameID", updatedGame.gameID());
                gameJson.addProperty("whitePlayer", updatedGame.whiteUsername());
                gameJson.addProperty("blackPlayer", updatedGame.blackUsername());
                gameJson.add("board", gson.toJsonTree(updatedGame.game().getBoard()));
                gameJson.addProperty("turn", updatedGame.game().getTeamTurn().toString());

                JsonObject loadGame = new JsonObject();
                loadGame.addProperty("serverMessageType", "LOAD_GAME");
                loadGame.add("game", gameJson);

                ctx.send(gson.toJson(loadGame));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClose(WsCloseContext ctx) {
        String sessionId = ctx.sessionId();
        sessions.remove(sessionId);
        System.out.println("WebSocket closed: " + sessionId);
    }
}
