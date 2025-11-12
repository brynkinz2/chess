package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;
import model.AuthData;
import java.io.*;
import java.net.*;
import java.util.List;


public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password) throws IOException {
        var request = new UserData(username, password);

        return makeRequest("/user", "POST", request, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws IOException {
        var request = new UserData(username, password);

        return makeRequest("/session", "POST", request, AuthData.class, null);
    }

    public void logout(String authToken) throws IOException {
        makeRequest("/session", "DELETE", null, AuthData.class, authToken);
    }

    public GameData createGame(String gameName, String authToken) throws IOException {
        var request = new GameData(0, null, null, gameName, new ChessGame());
        return makeRequest("/game", "POST", request, GameData.class, authToken);
    }

    public GamesList listGames(String authToken) throws IOException {
        return makeRequest("/game", "GET", null, GamesList.class, authToken);
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws IOException {
        var request = new JoinGameRequest(gameID, playerColor);
        makeRequest("/game", "PUT", request, null, authToken);
    }

    private <T> T makeRequest(String path, String method, Object request, Class<T> responseClass, String authToken) throws IOException {
        URL url = new URL(serverUrl + path);
        HttpURLConnection connection =  (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        // handle the header
        if (authToken != null) {
            connection.setRequestProperty("authorization", authToken);
        }
        // write the request body
        if (request != null) {
            connection.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(reqData.getBytes());
            }
        }

        // handle response
        connection.connect();
        int resCode = connection.getResponseCode();

        if (resCode == 200) {
            if (responseClass == null) {
                return null;
            }
            //parse the response
            try (InputStream is = connection.getInputStream()) {
                InputStreamReader isr = new InputStreamReader(is);
                return new Gson().fromJson(isr, responseClass);
            }
        } else {
            try (InputStream is = connection.getErrorStream()) {
//                InputStreamReader isr = new InputStreamReader(is);
                throw new IOException("Error: " + resCode);
            }
        }
    }

    public void clear() throws IOException {
        makeRequest( "/db","DELETE", null, null, null);
    }

    public record GamesList(List<GameData> games) {
        public int size() {
            return games.size();
        }
        public GameData get(int idx) {
            return games.get(idx);
        }
    }

    public record JoinGameRequest(int gameID, String playerColor) {}
}
