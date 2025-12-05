package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import ui.GameUI;
import websocket.commands.HeartbeatCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
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
                GameUI gameUI = (GameUI) ui;
                gameUI.updateBoard(msg.getGame());}
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                System.out.println("Server error: " + msg.getMessage());
            }
            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                GameUI gameUI = (GameUI) ui;
                gameUI.printNotification(msg.getMessage());
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

    public void sendLeave(String authToken, int gameID) {
        sendUserCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
    }


    public void sendResign(String authToken, int gameID) {
        sendUserCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
    }

    public void sendMove(String authToken, int gameID, ChessMove move) {
        try {
            MakeMoveCommand cmd = new MakeMoveCommand(authToken, gameID, move);
            String json = gson.toJson(cmd);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            System.out.println("Failed to send MAKE_MOVE command: " + e.getMessage());
        }
    }

    private void sendUserCommand(UserGameCommand.CommandType commandType, String authToken, int gameID) {
        try {
            UserGameCommand cmd = new UserGameCommand(commandType, authToken, gameID);
            String json = gson.toJson(cmd);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            System.out.println("Failed to send " + commandType + " command: " + e.getMessage());
        }
    }

    public void sendHeartbeat(String authToken, int gameID) {
        try {
            var cmd = new HeartbeatCommand(authToken, gameID);
            String json = gson.toJson(cmd);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            System.out.println("Failed to send heartbeat: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

}
