package client;

import com.google.gson.Gson;
import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import ui.GameUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

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
    public void onMessage(String json) {
        ServerMessage base = gson.fromJson(json, ServerMessage.class);

        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                ((GameUI) ui).updateBoard(msg.getGame());
            }
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                System.out.println("Server error: " + msg.getMessage());
            }
        }
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
