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
    private Map<String, String> userAuth = new HashMap<>();

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> clear());
        server.post("user", ctx -> register(ctx));
        server.post("/session", ctx -> login(ctx));
        server.delete("/session", ctx -> logout(ctx));

    }

    record UserData(
            String username,
            String password){
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
        UserData newUser = new UserData(username, password);
        users.put(username, newUser);
        userAuth.put(username, "xyz");
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
                userAuth.put(username, "xyz");
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
//        var req = serializer.fromJson(ctx.body(), String.class);
        String authToken = ctx.header("authorization");
        boolean found = false;
        String username = null;
        for (Map.Entry<String, String> entry : userAuth.entrySet()) {
            if (authToken.equals(entry.getValue())) {
                username = entry.getKey();
                userAuth.remove(entry.getKey());
                found = true;
                ctx.status(200);
                break;
            }
        }
        if (!found) {
            var errorRes = Map.of("message", "Error: User not logged in!");
        }
//        UserData currentUser = users.remove(username);
//        users.put(username, new UserData(username, currentUser.password));
        var res = Map.of("username", username, "authToken", " ");
        ctx.result(serializer.toJson(res));
        return;
    }

    private void clear() {
        users.clear();
        userAuth.clear();
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

}
