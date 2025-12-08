package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String authToken, Session session) {
        connections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>())
                .put(authToken, session);
    }

    public void remove(Session session) {
        for (var gameConnections : connections.values()) {
            gameConnections.values().removeIf(s -> s.equals(session));
        }
    }

    public void remove(Integer gameID, String authToken) {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            gameConnections.remove(authToken);
        }
    }

    public void broadcast(Integer gameID, String authToken, Notification notification) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }
        String json = new Gson().toJson(notification);
        for (var gameConnection : gameConnections.entrySet()) {
            if (!gameConnection.getKey().equals(authToken) && gameConnection.getValue().isOpen()) {
                gameConnection.getValue().getRemote().sendString(json);
            }
        }
    }

    public void broadcastToAll(Integer gameID, ServerMessage serverMessage) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }
        String json = new Gson().toJson(serverMessage);
        for (Session session : gameConnections.values()) {
            if (session.isOpen()) {
                session.getRemote().sendString(json);
            }
        }
    }
}