package server;

import com.google.gson.Gson;
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
            AuthData auth = userService.login(req.username(), req.password());
            if (auth == null) {
                ctx.status(401).result("{\"message\":\"Error: unauthorized\"}");
                return;
            }
            ctx.status(200).result(gson.toJson(auth));
        }
        catch (Exception e) {
            ctx.status(400).result("{\"message\":\"Error: bad request\"}");
        }
    }
}
