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
                    JsonObject error = new JsonObject();
                    error.addProperty("serverMessageType", "ERROR");
                    error.addProperty("errorMessage", "Error: invalid gameID");
                    ctx.send(gson.toJson(error));
                    return;
                }

                String color = null;
                if (game.whiteUsername() == null) {
                    color = "WHITE";
                }
                else if (game.blackUsername() == null) {
                    color = "BLACK";
                }

                if (color != null) {
                    try {
                        JoinGameRequest request = new JoinGameRequest(gameID, color);
                        gameService.joinGame(authToken, request);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        System.out.println("Join failed: " + e.getMessage());
                        return;
                    }
                }

                GameData updatedGame = gameService.getGameById(gameID);
                JsonObject loadGame = new JsonObject();
                loadGame.addProperty("serverMessageType", "LOAD_GAME");
                loadGame.add("game", gson.toJsonTree(updatedGame));
                ctx.send(gson.toJson(loadGame));

                for (WsConnectContext otherCtx : sessions.values()) {
                    if (!otherCtx.sessionId().equals(ctx.sessionId())) {
                        JsonObject notif = new JsonObject();
                        notif.addProperty("serverMessageType", "NOTIFICATION");

                        String joiningAs;
                        if (color == null) {
                            joiningAs = "SPECTATOR";
                        } else {
                            joiningAs = color;
                        }

                        notif.addProperty("message", username + " joined the game as " + joiningAs);
                        otherCtx.send(gson.toJson(notif));
                    }
                }

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
