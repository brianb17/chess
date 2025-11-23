package server;

import io.javalin.http.Context;
import service.UserService;

public class LogoutHandler {
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    public void logout(Context ctx) {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            ctx.status(401).result("{\"message\": \"Error: unauthorized\"}");
            return;
        }
        try {
            boolean success = userService.logout(authToken);
            if (!success) {
                ctx.status(401).result("{\"message\": \"Error: unauthorized\"}");
            } else {
                ctx.status(200).result("{}");
            }
        } catch (Exception e) {
            ctx.status(500).result("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }
}
