package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import datamodel.AuthData;
import datamodel.UserData;
import service.UserService;
import io.javalin.http.Context;

public class LoginHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    public void login(Context ctx) {
        try {
            UserData req = gson.fromJson(ctx.body(), UserData.class);

            // Check for missing fields
            if (req.username() == null || req.username().isEmpty() ||
                    req.password() == null || req.password().isEmpty()) {
                ctx.status(400).result("{\"message\":\"Error: bad request\"}");
                return;
            }

            AuthData auth = userService.login(req.username(), req.password());
            if (auth == null) {
                ctx.status(401).result("{\"message\":\"Error: unauthorized\"}");
                return;
            }

            ctx.status(200).result(gson.toJson(auth));
        } catch (JsonSyntaxException e) {
            // Invalid JSON → Bad Request
            ctx.status(400).result("{\"message\":\"Error: bad request\"}");
        } catch (Exception e) {
            // Anything unexpected → Internal Server Error
            ctx.status(500).result("{\"message\":\"Error: internal server error\"}");
        }
    }
}