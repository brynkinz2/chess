package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin server;
    private List<String> usernames = new ArrayList<>();

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));

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
        usernames.add(username);
        var res = Map.of("username", username, "authToken", "xyz");
        ctx.result(serializer.toJson(res));
    }

    private boolean userAlreadyExists(String username) {
        if (usernames.contains(username)) {
            return true;
        }
        return false;
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
