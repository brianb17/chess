package client;

import com.google.gson.Gson;
import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import websocket.commands.UserGameCommand;

import java.net.URI;


@ClientEndpoint
public class Websocket {
    private Session session;
    private final Gson gson = new Gson();
    private final Object ui;

    public Websocket(Object ui) {
        this.ui = ui;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket connection has been opened.");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received WebSocket Message");
        System.out.println(message);
    }

    public void connect(String authToken, int gameID) throws Exception {
        ClientManager client = ClientManager.createClient();
        client.connectToServer(this, new URI("ws://localhost:8080/ws"));
        sendConnect(authToken, gameID);
    }

    private void sendConnect(String authToken, int gameID) {
        try {
            UserGameCommand cmd = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    authToken,
                    gameID
            );
            String json = gson.toJson(cmd);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            System.out.println("Failed to send CONNECT command: " + e.getMessage());
        }
    }



    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
    }
}
