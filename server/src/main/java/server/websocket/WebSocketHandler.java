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
        } catch (Exception ex) {

        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        connections.remove(ctx.session);
        System.out.println("Connection closed");
    }

    public void connect() {

    }
}
