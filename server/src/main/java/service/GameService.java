package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
//import model.AuthData;

import javax.xml.crypto.Data;
import java.util.List;

public class GameService {
    private final DataAccess dataAccess;
    private int nextGameID = 100;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResult createGame(String authToken, String gameName) throws DataAccessException {
        if (gameName == null) {
            throw new DataAccessException("bad request: Please provide a game name.");
        }
        verifyUser(authToken);
        int currGameID = nextGameID;
        ChessGame newGame = new ChessGame();
        dataAccess.createGame(new GameData(currGameID, null, null, gameName, newGame));
        nextGameID++;
        return new CreateGameResult(currGameID);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        String newPlayer = verifyUser(authToken);
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("bad request : Game does not exist.");
        }
        GameData updatedGame;
        if (playerColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Player color already taken");
            }
            updatedGame = new GameData(gameID, newPlayer, game.blackUsername(), game.gameName(), game.game());
        }
        else if (playerColor.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Player color already taken");
            }
            updatedGame = new GameData(gameID, game.whiteUsername(), newPlayer, game.gameName(), game.game());
        }
        else {
            throw new DataAccessException("bad request : Invalid player color.");
        }
        dataAccess.update(updatedGame);
    }

    public ListGameResult listGames(String authToken) throws DataAccessException {
        String user = verifyUser(authToken);
        if (user == null) {
            throw new DataAccessException("User unauthorized.");
        }
        return new ListGameResult(dataAccess.listGames());
    }

    private String verifyUser(String authToken) throws DataAccessException {
        AuthData user = dataAccess.getAuth(authToken);
        if (user == null) {
            throw new DataAccessException("User unauthorized");
        }
        return user.username();
    }

    public record CreateGameResult(int gameID) {}
    public record ListGameResult(List<GameData> games) {}
}
