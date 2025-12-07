package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.MySQLDataAccess;
import model.*;
import service.*;
import server.websocket.WebSocketHandler;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private Map<String, UserData> users = new HashMap<>();
    private Map<String, String> authTokens = new HashMap<>();
    private List<GameData> games = new ArrayList<>();
    private int currGame = 100;
    private int currAuth = 0;

    private final WebSocketHandler webSocketHandler;

    public Server() {
        MySQLDataAccess dataAccess = null;
        try {
            dataAccess = new MySQLDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);

        webSocketHandler = new WebSocketHandler(dataAccess);


        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register WebSocket endpoint FIRST
        server.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler::handleConnect);
            ws.onMessage(webSocketHandler::handleMessage);
            ws.onClose(webSocketHandler::handleClose);
        });


        server.delete("db", this::handleClear);
        server.post("user", this::handleRegister);
        server.post("/session", this::handleLogin);
        server.delete("/session", this::handleLogout);
        server.post("/game", this::handleCreateGame);
        server.put("/game", this::handleJoinGame);
        server.get("/game", this::handleListGames);

    }

    private void handleRegister(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        try {
            var result = userService.register(
                    (String) req.get("username"),
                    (String) req.get("password")
            );
            ctx.status(200);
            ctx.result(serializer.toJson(result));
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleLogin(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        try {
            var result = userService.login(
                    (String) req.get("username"),
                    (String) req.get("password")
            );
            ctx.status(200);
            ctx.result(serializer.toJson(result));
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleLogout(Context ctx) {
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            userService.logout(authToken);
            ctx.status(200);
            return;
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleCreateGame(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String authToken = ctx.header("authorization");
        try {
            String gameName = (String) req.get("gameName");
            var result = gameService.createGame(authToken, gameName);
            ctx.status(200);
            ctx.result(serializer.toJson(result));
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleJoinGame(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String authToken = ctx.header("authorization");
        try {
            // Handle gameID (could be Double or Integer from Gson)
            int gameID;
            Object gameIDObj = req.get("gameID");
            if (gameIDObj instanceof Double) {
                gameID = ((Double) gameIDObj).intValue();
            } else if (gameIDObj instanceof Integer) {
                gameID = (Integer) gameIDObj;
            } else {
                throw new DataAccessException("bad request: Please include a game ID");
            }
            String playerColor = (String) req.get("playerColor");
            if (playerColor == null) {
                throw new DataAccessException("bad request: Please include a player color");
            }
            gameService.joinGame(authToken, gameID, playerColor);
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
                handleError(ctx, e);
        }
    }

    private void handleListGames(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String authToken = ctx.header("authorization");
        try {
            var result = gameService.listGames(authToken);
            ctx.status(200);
            ctx.result(serializer.toJson(result));
        } catch (DataAccessException e) {
            handleError(ctx, e);
        }
    }

    private void handleError(Context ctx, DataAccessException e) {
        var serializer = new Gson();
        if (e.getMessage().contains("already")) {
            ctx.status(403);
        } else if (e.getMessage().contains("unauthorized")) {
            ctx.status(401);
        } else if (e.getMessage().contains("bad request") || e.getMessage().contains("required")) {
            ctx.status(400);
        } else {
            ctx.status(500);
        }
        ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
    }

    private void handleClear(Context ctx) throws DataAccessException {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            handleError(ctx, e);
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
