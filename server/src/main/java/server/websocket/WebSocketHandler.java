package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.core.server.WebSocketCreator;
import websocket.commands.*;
import websocket.messages.*;
import chess.ChessGame;
import chess.ChessMove;
import dataaccess.*;
import model.*;

import java.io.IOException;
import websocket.messages.Error;

public class WebSocketHandler implements WsConnectHandler, WsCloseHandler, WsMessageHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private MySQLDataAccess dataAccess;

    public WebSocketHandler(MySQLDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command, ctx.session);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        connections.remove(ctx.session);
        System.out.println("Connection closed");
    }

    public void connect(UserGameCommand command, Session session) {
        try {
            // Validate and get user info
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                throw new DataAccessException("Invalid auth token");
            }
            String username = auth.username();

            // Validate game exists
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Invalid game ID");
            }
//            ChessGame game = gameData.game();

            // Add connection
            connections.add(command.getGameID(), command.getAuthToken(), session);

            // Send LOAD_GAME to root client
            LoadGame loadGameMsg = new LoadGame(gameData.game());
            session.getRemote().sendString(new Gson().toJson(loadGameMsg));

            // Determine if player or observe
            String playerColor = determinePlayerColor(gameData, username);
            String notificationText;
            if (playerColor == null) {
                notificationText = String.format("%s has joined as an observer", username);
            }
            else {
                notificationText = String.format("%s has joined the game as %s", username, playerColor);
            }

            // Send NOTIFICATION to all other clients
            Notification notification = new Notification(notificationText);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);


        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void sendError(Session session, String message) {
        try {
            Error error = new Error(message);
            session.getRemote().sendString(new Gson().toJson(error));
        } catch (IOException e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }

    private String determinePlayerColor(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return "WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            return "BLACK";
        }
        return null;
    }
}
