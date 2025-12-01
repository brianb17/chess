package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import datamodel.GameData;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import service.GameService;
import service.UserService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private static final Map<String, WsConnectContext> sessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    public static UserService userService;
    public static GameService gameService;

    public static void onConnect(WsConnectContext ctx) {
        sessions.put(ctx.sessionId(), ctx);
        System.out.println("WebSocket connected " + ctx.sessionId());
    }

    public static void onMessage(WsMessageContext ctx) {
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
                    return;
                }

                GameData game = gameService.getGameById(gameID);
                if (game == null) {
                    return;
                }


                JsonObject loadGame = new JsonObject();
                loadGame.addProperty("type", "LOAD_GAME");
                loadGame.addProperty("gameID", gameID);
                loadGame.addProperty("whitePlayer", game.whiteUsername());
                loadGame.addProperty("blackPlayer", game.blackUsername());
                loadGame.add("board", gson.toJsonTree(game.game().getBoard()));
                loadGame.addProperty("turn", game.game().getTeamTurn().toString());

                ctx.send(gson.toJson(loadGame));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onClose(WsCloseContext ctx) {
        String sessionId = ctx.sessionId();
        sessions.remove(sessionId);
        System.out.println("WebSocket closed: " + sessionId);
    }
}
