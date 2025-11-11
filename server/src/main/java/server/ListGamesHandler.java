package server;

import io.javalin.http.Context;
import service.GameService;
import com.google.gson.Gson;
import java.util.Map;

public class ListGamesHandler {
    private final GameService gameService;
    private final Gson gson; // Gson instance for serialization

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson();
    }

    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");

        // Check for missing or empty header
        if (authToken == null || authToken.isEmpty()) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
            return;
        }

        try {
            Object result = gameService.listGames(authToken);

            // Serialize result manually using Gson
            String json = gson.toJson(result);

            ctx.status(200)
                    .contentType("application/json")
                    .result(json);

        } catch (Exception e) {
            // Handle unauthorized and other errors
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase().contains("unauthorized")) {
                ctx.status(401)
                        .result(gson.toJson(Map.of("message", "Error: unauthorized")));
            } else {
                ctx.status(500)
                        .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        }
    }
}
