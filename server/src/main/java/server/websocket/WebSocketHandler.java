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
                case MAKE_MOVE -> makeMove(command, ctx.session, ctx.message());
                case RESIGN -> resign(command, ctx.session);
                case LEAVE -> leave(command, ctx.session);
            }
        } catch (Exception e) {
            sendError(ctx.session, e.getMessage());
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

    private void makeMove(UserGameCommand command, Session session, String message) {
        try {
            // Parse move command
            MakeMoveCommand makeMoveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
            ChessMove move = makeMoveCommand.getMove();

            // Validate user
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                throw new DataAccessException("Invalid auth token");
            }
            String username = auth.username();

            // Get game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Invalid game ID");
            }
            ChessGame game = gameData.game();

            // Check if game is over
            if (game.isGameOver() || isGameOver(game)) {
                throw new IllegalStateException("Game is over");
            }

            // Verify player is making the move
            String playerColor = determinePlayerColor(gameData, username);
            if (playerColor == null) {
                throw new IllegalStateException("Observers cannot make moves");
            }

            // Verify it's their turn
            ChessGame.TeamColor playerTurn = gameData.game().getTeamTurn();
            if (!playerColor.equalsIgnoreCase(playerTurn.toString())) {
                throw new IllegalStateException("It is not your turn");
            }

            // Make the move (throws InvalidMoveException if illegal)
            game.makeMove(move);

            // Update game in database
            dataAccess.update(gameData);

            // Send LOAD_GAME to all clients
            LoadGame loadGameMsg = new LoadGame(game);
            connections.broadcastToAll(gameData.gameID(), loadGameMsg);

            // Send move NOTIFICATION to other clients
            String moveText = String.format("%s has moved from %s to %s",
                    username,
                    move.getStartPosition(),
                    move.getEndPosition()
                    );
            Notification notification = new Notification(moveText);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

            // Check for check, checkmate, stalemate
            ChessGame.TeamColor opponentColor = (playerTurn == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponentColor)) {
                String checkmate = String.format("%s is in checkmate", opponentColor);
                connections.broadcastToAll(command.getGameID(), new Notification(checkmate));
            } else if (game.isInCheckmate(opponentColor)) {
                connections.broadcastToAll(command.getGameID(), new Notification("Stalemate!"));
            } else if (game.isInCheck(opponentColor)) {
                String check = String.format("%s is in check", opponentColor);
                connections.broadcastToAll(command.getGameID(), new Notification(check));
            }

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void leave(UserGameCommand command, Session session) {
        try {
            // Validate user
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                throw new DataAccessException("Invalid auth token");
            }
            String username = auth.username();

            // Get game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData != null) {
                // If player, remove them
                GameData updatedGame = removePlayer(username, gameData);
                dataAccess.update(updatedGame);
            }
            else {
                throw new DataAccessException("Invalid game ID");
            }

            // Notify other clients
            String message = String.format("%s has left the game", username);
            Notification notification = new Notification(message);
            connections.broadcast(command.getGameID(), command.getAuthToken(), notification);

            // Remove connection
            connections.remove(command.getGameID(), command.getAuthToken());

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void resign(UserGameCommand command, Session session) {
        try {
            // Validate user
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                throw new DataAccessException("Invalid auth token");
            }
            String username = auth.username();

            // Get game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Invalid game ID");
            }
            ChessGame game = gameData.game();

            // Verify player is resigning
            String playerColor = determinePlayerColor(gameData, username);
            if (playerColor == null) {
                throw new IllegalStateException("Observers cannot resign");
            }

            // Check if game is over
            if (game.isGameOver() || isGameOver(game)) {
                throw new IllegalStateException("Game is already over");
            }

            GameData updated = removePlayer(username, gameData);
            updated.game().setGameOver();
            dataAccess.update(updated);

            // Notify other clients
            String message = String.format("%s has resigned from the game", username);
            Notification notification = new Notification(message);
            connections.broadcastToAll(command.getGameID(), notification);

            // Remove connection
            connections.remove(command.getGameID(), command.getAuthToken());



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

    // helper functions

    private String determinePlayerColor(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return "WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            return "BLACK";
        }
        return null;
    }

    private boolean isGameOver(ChessGame game) {
        return game.isInCheckmateHelper(ChessGame.TeamColor.WHITE, game.getTeamTurn()) ||
                game.isInCheckmateHelper(ChessGame.TeamColor.BLACK, game.getTeamTurn()) ||
                game.isInStalemateHelper(ChessGame.TeamColor.WHITE, game.getTeamTurn()) ||
                game.isInStalemateHelper(ChessGame.TeamColor.BLACK, game.getTeamTurn());
    }

    private GameData removePlayer(String username, GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        }
        else if (username.equals(gameData.blackUsername())) {
            return new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        }
        return gameData;
    }
}
