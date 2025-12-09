package client;

import com.google.gson.Gson;
import datamodel.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String baseUrl;
    private final Gson gson = new Gson();

    // Private class to correctly parse the server's JSON error body: {"message":"..."}
    private static class ServerErrorResponse {
        private String message;
        public String getMessage() {
            return message;
        }
    }

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    // --- PreLogin ---

    public void clear() throws Exception {
        sendDelete("/db", null);
    }

    public AuthData register(String username, String password, String email) throws Exception {
        RegisterRequest req = new RegisterRequest(username, password, email);
        String jsonReq = gson.toJson(req);

        String jsonResp = sendPost("/user", jsonReq, null);
        return gson.fromJson(jsonResp, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        UserData user = new UserData(username, "", password);
        String jsonReq = gson.toJson(user);

        String jsonResp = sendPost("/session", jsonReq, null);
        return gson.fromJson(jsonResp, AuthData.class);
    }

    // --- PostLogin ---

    public void logout(String authToken) throws Exception {
        sendDelete("/session", authToken);
    }

    public int createGame(String authToken, String gameName) throws Exception {
        String jsonReq = gson.toJson(Map.of("gameName", gameName));
        String jsonResp = sendPost("/game", jsonReq, authToken);

        // Gson parses numbers as Double, so we cast to Double then get int value
        Map<?, ?> map = gson.fromJson(jsonResp, Map.class);
        return ((Double) map.get("gameID")).intValue();
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        String jsonResp = sendGet("/game", authToken);
        return gson.fromJson(jsonResp, ListGamesResult.class);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        JoinGameRequest req = new JoinGameRequest(gameID, playerColor);
        String jsonReq = gson.toJson(req);
        sendPut("/game", jsonReq, authToken);
    }

    // --- HTTP Helpers ---

    private String sendGet(String path, String authToken) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (authToken != null) {
            conn.setRequestProperty("authorization", authToken);
        }

        return getResponse(conn);
    }

    private String sendPost(String path, String json, String authToken) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            conn.setRequestProperty("authorization", authToken);
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        return getResponse(conn);
    }

    private String sendPut(String path, String json, String authToken) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            conn.setRequestProperty("authorization", authToken);
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        return getResponse(conn);
    }

    private void sendDelete(String path, String authToken) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        if (authToken != null) {
            conn.setRequestProperty("authorization", authToken);
        }

        getResponse(conn);
    }

    private String getResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        // Determine whether to read from the input stream (2xx success) or error stream (4xx/5xx error)
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String responseBody = sb.toString();

            if (code >= 200 && code < 300) {
                return responseBody;
            } else {
                // FIX: Parse the JSON error body and throw an exception with ONLY the message.
                if (!responseBody.isEmpty()) {
                    ServerErrorResponse error = gson.fromJson(responseBody, ServerErrorResponse.class);

                    // Throw an exception containing ONLY the clean message (e.g., "Error: unauthorized")
                    throw new Exception(error.getMessage());
                } else {
                    // Handle cases where server returns an error status with an empty body
                    throw new Exception("Server returned HTTP " + code);
                }
            }
        }
    }
}