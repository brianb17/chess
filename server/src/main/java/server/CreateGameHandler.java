package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import dataaccess.DataAccessException;

import java.util.Map;

public class CreateGameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            String gameName = body.get("gameName");

            int gameId = gameService.createGame(authToken, gameName);

            ctx.status(200);
            ctx.result(gson.toJson(Map.of("gameID", gameId)));

        } catch (DataAccessException e) {
            String msg = e.getMessage();
            if (msg.contains("unauthorized")) {
                ctx.status(401);
            }
            else if (msg.contains("bad request")) {
                ctx.status(400);
            }
            else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(Map.of("message", "Error: " + msg)));

        } catch (Exception e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
