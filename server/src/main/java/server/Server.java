package server;

import model.GameData;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin server;
    private Map<String, UserData> users = new HashMap<>();
    private Map<String, String> userAuth = new HashMap<>();
    private List<GameData> games = new ArrayList<>();
    private int currGame = 100;
    private int currAuth = 0;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> clear());
        server.post("user", ctx -> register(ctx));
        server.post("/session", ctx -> login(ctx));
        server.delete("/session", ctx -> logout(ctx));
        server.post("/game", ctx -> createGame(ctx));
        server.put("/game", ctx -> joinGame(ctx));
        server.get("/game", ctx -> listGames(ctx));

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
            var errorRes = Map.of("message", "Error: bad request");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        if (userAlreadyExists(username)) {
            ctx.status(403);
            var errorRes = Map.of("message", "Error: user already taken");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        UserData newUser = new UserData(username, password);
        users.put(username, newUser);
        String currUserAuth = String.valueOf(currAuth);
        userAuth.put(username, currUserAuth);
        currAuth++;
        var res = Map.of("username", username, "authToken", currUserAuth);
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
                String currUserAuth = String.valueOf(currAuth);
                userAuth.put(username, currUserAuth);
                currAuth++;
                var res = Map.of("username", username, "authToken", currUserAuth);
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

    private String userLoggedIn(String authToken) {
        String username = null;
        for (Map.Entry<String, String> entry : userAuth.entrySet()) {
            if (authToken.equals(entry.getValue())) {
                username = entry.getKey();
                break;
            }
        }
        return username;
    }

    private void logout(Context ctx) {
        var serializer = new Gson();
//        var req = serializer.fromJson(ctx.body(), String.class);
        String authToken = ctx.header("authorization");
        boolean found = false;
        String username = userLoggedIn(authToken);
        if (username == null) {
            ctx.status(401);
            var errorRes = Map.of("message", "Error: User not logged in!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
//        UserData currentUser = users.remove(username);
//        users.put(username, new UserData(username, currentUser.password));
        userAuth.remove(username);
        var res = Map.of("username", username, "authToken", " ");
        ctx.result(serializer.toJson(res));
        return;
    }


    private void createGame(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String authToken = ctx.header("authorization");
        String username = userLoggedIn(authToken);
        if (username == null) {
            ctx.status(401);
            var errorRes = Map.of("message", "Error: User not logged in!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        String gameName = (String) req.get("gameName");
        if (gameName == null) {
            ctx.status(400);
            var errorRes = Map.of("message", "Error: Game name is required!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        int gameID = currGame;
        GameData newGame = new GameData(gameID, null, null, gameName);
        games.add(newGame);
        ctx.status(200);
        var res = Map.of("gameID", gameID);
        ctx.result(serializer.toJson(res));
        currGame++;
        return;
    }

    private void joinGame(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String authToken = ctx.header("authorization");
        String username = userLoggedIn(authToken);
        if (username == null) {
            ctx.status(401);
            var errorRes = Map.of("message", "Error: User not logged in!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        int gameID;
        try {
            Object gameIDObj = req.get("gameID");
            if (gameIDObj instanceof Double) {
                gameID = ((Double) gameIDObj).intValue();
            } else if (gameIDObj instanceof Integer) {
                gameID = (Integer) gameIDObj;
            } else {
                throw new IllegalArgumentException("Invalid gameID type");
            }
        } catch (Exception e) {
            ctx.status(400);
            var errorRes = Map.of("message", "Error: Please include a game ID!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        GameData thisGame = null;
        for (GameData gameData : games) {
            if (gameData.getGameID() == gameID) {
                thisGame = gameData;
                games.remove(gameData);
                break;
            }
        }
        if (thisGame == null) {
            ctx.status(400);
            var errorRes = Map.of("message", "Error: Game not found!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        String playerColor = (String) req.get("playerColor");
        if (playerColor == null) {
            ctx.status(400);
            var errorRes = Map.of("message", "Error: Invalid color");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        if (playerColor.equals("BLACK")) {
            if (thisGame.getBlackUsername() != null) {
                ctx.status(403);
                var errorRes = Map.of("message", "Error: Player color taken!");
                ctx.result(serializer.toJson(errorRes));
                return;
            }
            GameData newGame = new GameData(gameID, thisGame.getWhiteUsername(), username, thisGame.getGameName());
            games.add(newGame);
        }
        else if (playerColor.equals("WHITE")) {
            if (thisGame.getWhiteUsername() != null) {
                ctx.status(403);
                var errorRes = Map.of("message", "Error: Player color taken!");
                ctx.result(serializer.toJson(errorRes));
                return;
            }
            GameData newGame = new GameData(gameID, username, thisGame.getBlackUsername(), thisGame.getGameName());
            games.add(newGame);
        }
        return;
    }

    private void listGames(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        String authToken = ctx.header("authorization");
        String username = userLoggedIn(authToken);
        if (username == null) {
            ctx.status(401);
            var errorRes = Map.of("message", "Error: User not logged in!");
            ctx.result(serializer.toJson(errorRes));
            return;
        }
        var res = Map.of("games", games);
        ctx.result(serializer.toJson(res));
        return;
    }

    private void clear() {
        users.clear();
        userAuth.clear();
        games.clear();
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

}
