package client;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.*;
import jakarta.websocket.*;
import websocket.messages.Error;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// Extend Endpoint for WebSocket to work properly
public class WebSocketSender extends Endpoint {

    private Session session;
    private NotificationHandler notificationHandler;

    public WebSocketSender(String url, NotificationHandler notificationHandler) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // Set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {
                        // Parse base message to determine type
                        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

                        // Deserialize to correct subclass
                        switch (serverMessage.getServerMessageType()) {
                            case LOAD_GAME -> {
                                LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
                                notificationHandler.notify(loadGame);
                            }
                            case NOTIFICATION -> {
                                Notification notification = new Gson().fromJson(message, Notification.class);
                                notificationHandler.notify(notification);
                            }
                            case ERROR -> {
                                Error error = new Gson().fromJson(message, Error.class);
                                notificationHandler.notify(error);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error handling message: " + e.getMessage());
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception("Failed to connect to WebSocket: " + ex.getMessage());
        }
    }

    // Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID) throws Exception {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Failed to send connect: " + ex.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        try {
            MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Failed to send move: " + ex.getMessage());
        }
    }

    public void leave(String authToken, int gameID) throws Exception {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Failed to send leave: " + ex.getMessage());
        }
    }

    public void resign(String authToken, int gameID) throws Exception {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new Exception("Failed to send resign: " + ex.getMessage());
        }
    }
}