package websocket.commands;

public class HeartbeatCommand {
    private final String type = "HEARTBEAT";
    private final String authToken;
    private final int gameID;

    public HeartbeatCommand(String authToken, int gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public String getType() {
        return type;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getGameID() {
        return gameID;
    }
}
