//do handler part of ListGames if needed and then the server part. The service is already done.

package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();

        userService = new UserService(dataAccess);
        var clearService = new ClearService(dataAccess);
        var gameService = new GameService(dataAccess);

        var clearHandler = new ClearHandler(clearService);
        var loginHandler = new LoginHandler(userService);
        var logoutHandler = new LogoutHandler(userService);
        var createGameHandler = new CreateGameHandler(gameService);
        var listGamesHandler = new ListGamesHandler(gameService);


        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", clearHandler::clear);
        server.post("user", this::register);
        server.post("session", loginHandler::login);
        server.delete("session", logoutHandler::logout);
        server.post("game", createGameHandler::createGame);
        server.get("/game", listGamesHandler::listGames);
        // Register your endpoints and exception handlers here.

    }

    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var authData = userService.register(user);

            ctx.result(serializer.toJson(authData));
        } catch (IllegalArgumentException ex) {
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(msg);
        }
        catch (Exception ex) {
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(msg);
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
