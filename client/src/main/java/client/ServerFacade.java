package client;

import com.google.gson.Gson;
import model.UserData;
import model.AuthData;
import java.io.*;
import java.net.*;



public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password) throws IOException {
        var request = new UserData(username, password);

        return makeRequest("/user", "POST", request, AuthData.class);
    }

    public AuthData login(String username, String password) throws IOException {
        var request = new UserData(username, password);

        return makeRequest("/session", "POST", request, AuthData.class);
    }

    private <T> T makeRequest(String path, String method, Object request, Class<T> responseClass) throws IOException {
        URL url = new URL(serverUrl + path);
        HttpURLConnection connection =  (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(true);

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
                InputStreamReader isr = new InputStreamReader(is);
                throw new IOException("Error: " + resCode);
            }
        }
    }

    public void clear() throws IOException {
        makeRequest( "/db","DELETE", null, null);
    }
}
