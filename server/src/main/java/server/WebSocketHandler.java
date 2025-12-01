package server;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private static final Map<String, WsConnectContext> sessions = new ConcurrentHashMap<>();

    public static void onConnect(WsConnectContext ctx) {
        sessions.put(ctx.sessionId(), ctx);
        System.out.println("WebSocket connected " + ctx.sessionId());
    }

    public static void onMessage(WsMessageContext ctx) {
        System.out.println("Websocket message received: " + ctx.message());
    }

    public static void onClose(WsCloseContext ctx) {
        String sessionId = ctx.sessionId();
        sessions.remove(sessionId);
        System.out.println("WebSocket closed: " + sessionId);
    }
}
