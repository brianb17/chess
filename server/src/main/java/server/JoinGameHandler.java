package server;

import com.google.gson.Gson;
import datamodel.JoinGameRequest;
import service.GameService;
import dataaccess.DataAccessException;

import io.javalin.http.Context;

public class JoinGameHandler {

    private final GameService gameService;
    private final Gson gson;

    public JoinGameHandler(GameService gameService, Gson gson) {
        this.gameService = gameService;
        this.gson = gson;
    }

    public void joinsgame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isEmpty()) {
                // Correct 401 response: JSON structure required
                ctx.status(401).json("{\"message\": \"Error: unauthorized\"}");
                return;
            }

            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, request);

            // Success case
            ctx.status(200).json("{}");
        }
        catch (DataAccessException e) {
            String msg = e.getMessage().toLowerCase();
            String responseMessage;
            int status = 500;

            // Map the DataAccessException messages to specific HTTP status codes and EXACT JSON bodies
            if (msg.contains("unauthorized")) {
                status = 401;
                responseMessage = "Error: unauthorized";
            }
            else if (msg.contains("bad request")) {
                status = 400;
                responseMessage = "Error: bad request";
            }
            else if (msg.contains("already taken")) {
                status = 403;
                responseMessage = "Error: already taken";
            }
            else {
                // Catch-all: Must include "Error: " prefix
                status = 500;
                responseMessage = "Error: " + e.getMessage();
            }

            // 🛑 FIX: Use the required JSON structure for the response body
            ctx.status(status).json("{\"message\": \"" + responseMessage + "\"}");
        }
        catch (Exception e) {
            // Catch all unexpected errors (e.g., Gson parsing failures)
            // 🛑 FIX: Use the required JSON structure and "Error: " prefix
            ctx.status(500).json("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }
}