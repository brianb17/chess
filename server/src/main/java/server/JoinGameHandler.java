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
                ctx.status(401).json("{\"message\": \"Error: unauthorized\"}");
                return;
            }

            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, request);


            ctx.status(200).json("{}");
        }
        catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("unauthorized")) {
                ctx.status(401).json("{\"message\": \"" + msg + "\"}");
            }
            else {
                ctx.status(400).json("{\"message\": \"" + msg + "\"}");
            }
        }
        catch (IllegalStateException e) {
            ctx.status(403).json("{\"message\": \"" + e.getMessage() + "\"}");
        }
        catch (DataAccessException e) {
            ctx.status(500).json("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
        catch (Exception e) {
            ctx.status(500).json("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }
}
