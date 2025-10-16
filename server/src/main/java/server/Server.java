package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin server;
    private Map<String, UserData> users = new HashMap<>();

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));
        server.post("/session", ctx -> login(ctx));
        server.delete("/session", ctx -> logout(ctx));

    }

    record UserData(
            String username,
            String password,
            String authToken){
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        if (password == null) {
            ctx.status(400);
            var errorRes = Map.of("message", "Error: Please include a password.");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        if (userAlreadyExists(username)) {
            ctx.status(403);
            var errorRes = Map.of("message", "Error: Username already exists!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        UserData newUser = new UserData(username, password, "xyz");
        users.put(username, newUser);
        var res = Map.of("username", username, "authToken", "xyz");
        ctx.result(serializer.toJson(res));
    }

    private boolean userAlreadyExists(String username) {
        if (users.containsKey(username)) {
            return true;
        }
        return false;
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        if (username == null || password == null) {
            ctx.status(400);
            var errorRes = Map.of("message", "Error: Please include a username and password.");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        if (userAlreadyExists(username)) {
            if (users.get(username).password.equals(password)) {
                var res = Map.of("username", username, "authToken", "xyz");
                ctx.result(serializer.toJson(res));
                return;
            }
            else {
                ctx.status(401);
                var errorRes = Map.of("message", "Error: Username and password do not match!");
                ctx.result(serializer.toJson(errorRes));
                return;
            }
        }
        else {
            ctx.status(401);
            var errorRes = Map.of("message", "Error: Username does not exist!");
            ctx.result(serializer.toJson(errorRes));
        }
    }

    private void logout(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        return;
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

}
