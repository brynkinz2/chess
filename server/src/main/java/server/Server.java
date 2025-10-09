package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;

import java.util.Map;

public class Server {

    private final Javalin server;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));

    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        //
        var res = Map.of("username", req.get("username"), "authToken", "xyz");
        ctx.result(serializer.toJson(res));

    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
