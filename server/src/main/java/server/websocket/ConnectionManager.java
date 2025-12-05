package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
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

    public void broadcast(Integer gameID, String authToken, Session session) {}
}