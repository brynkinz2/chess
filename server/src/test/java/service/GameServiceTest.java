package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private String authToken;


    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess =  new MemoryDataAccess();
        gameService = new GameService(dataAccess);
        userService = new UserService(dataAccess);
        dataAccess.clear();

        // Create a user and get auth token for tests
        var result = userService.register("testuser", "password123");
        authToken = result.authToken();
    }

    // CREATE GAME TESTS
    @Test
    public void createValidGame() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);

        assertNotNull(result.gameID());

        GameData thisGame = dataAccess.getGame(result.gameID());
        assertNotNull(thisGame);
    }

    @Test
    public void creatGameInvalidUser() throws DataAccessException {
        String gameName = "testgame";
        assertThrows(DataAccessException.class, () -> gameService.createGame("fakeAuth", gameName));
    }

    @Test
    public void creatGameInvalidGame() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> gameService.createGame(authToken, null));
    }

    // Join Tests
    @Test
    public void joinGameAsWhite() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);
        GameData thisGame = dataAccess.getGame(result.gameID());
        String username = dataAccess.getAuth(authToken).username();

        gameService.joinGame(authToken, thisGame.gameID(), "WHITE");
        GameData updatedGame = new GameData(thisGame.gameID(), username, thisGame.blackUsername(), thisGame.gameName());

        assertEquals(dataAccess.getGame(thisGame.gameID()), updatedGame);
    }

    @Test
    public void joinGameAsBlack() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);
        GameData thisGame = dataAccess.getGame(result.gameID());
        String username = dataAccess.getAuth(authToken).username();

        gameService.joinGame(authToken, thisGame.gameID(), "BLACK");
        GameData updatedGame = new GameData(thisGame.gameID(), thisGame.whiteUsername(), username, thisGame.gameName());

        assertEquals(dataAccess.getGame(thisGame.gameID()), updatedGame);
    }

    @Test
    public void joinGameAsInvalidColor() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);
        GameData thisGame = dataAccess.getGame(result.gameID());

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, thisGame.gameID(), "color"));
    }

    @Test
    public void invalidJoinAsBlack() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);
        GameData thisGame = dataAccess.getGame(result.gameID());
        String username = dataAccess.getAuth(authToken).username();
        // Join should work first time
        gameService.joinGame(authToken, thisGame.gameID(), "BLACK");
        GameData updatedGame = new GameData(thisGame.gameID(), thisGame.whiteUsername(), username, thisGame.gameName());
        // throws exception on second join
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, thisGame.gameID(), "BLACK"));
    }

    @Test
    public void invalidJoinGameAsWhite() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);
        GameData thisGame = dataAccess.getGame(result.gameID());
        String username = dataAccess.getAuth(authToken).username();
        // Join should work first time
        gameService.joinGame(authToken, thisGame.gameID(), "WHITE");
        GameData updatedGame = new GameData(thisGame.gameID(), username, thisGame.blackUsername(), thisGame.gameName());
        // throws exception on second join
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, thisGame.gameID(), "WHITE"));
    }

    @Test
    public void joinInvalidGameID() throws DataAccessException {
        String gameName = "testgame";
        var result = gameService.createGame(authToken, gameName);
        GameData thisGame = dataAccess.getGame(result.gameID());
        String username = dataAccess.getAuth(authToken).username();

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 95, "WHITE"));
    }

    // List Games Test
    @Test
    public void listGamesMultipleGames () throws DataAccessException {
        // create multiple games
        gameService.createGame(authToken, "testgame1");
        gameService.createGame(authToken, "testgame2");
        gameService.createGame(authToken, "testgame3");
        // get games list
        var result = gameService.listGames(authToken);
        assertNotNull(result);
        assertEquals(3, result.games().size());
    }

    @Test
    public void listGamesEmpty () throws DataAccessException {
        var result = gameService.listGames(authToken);
        assertNotNull(result);
        assertEquals(0, result.games().size());
    }

    @Test
    public void listGamesUnauthorized () throws DataAccessException {
        assertThrows(DataAccessException.class, () -> gameService.listGames("fakeAuth"));
    }

}
