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
//            switch (command.getCommandType()) {
//                case MAKE_MOVE ->
//            }
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
            // Send NOTIFICATION to all other clients


        } catch (Exception e) {

        }
    }
}
