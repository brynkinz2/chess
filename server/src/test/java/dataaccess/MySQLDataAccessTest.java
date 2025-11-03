package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLDataAccessTest {
    private DataAccess dataAccess;

    @BeforeEach
    public void setUp() throws DataAccessException {
        // Initialize the database before each test
        dataAccess = new MySQLDataAccess();
        // Clear the database to start fresh
        dataAccess.clear();
    }

    //---------CLEAR TESTS--------
//    @Test
//    public void clearSuccess() throws DataAccessException {
//        // Add stuff into tables first
//        dataAccess.createUser(new UserData("test", "pass"));
//        // Make sure it shows up
//        assertNotNull(dataAccess.getUser("test"));
//        dataAccess.clear();
//
////        assertNull(dataAccess);
//    }

    //USER TESTS
    @Test
    public void createUserSuccess() throws DataAccessException {
        //Create a user
        dataAccess.createUser(new UserData("test", "pass"));
        //Check to see if it was successfully added to tables
        UserData user = dataAccess.getUser("test");
        assertNotNull(user);
        assertEquals(user.username(), "test");
        // password encrypted so make sure it exists
        assertNotNull(user.password());
    }

    @Test
    public void createUserFailure() throws DataAccessException {
        //Create user twice
        UserData user1 = new UserData("test", "pass");
        dataAccess.createUser(user1);

        UserData user2 = new UserData("test", "pass");
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user2));
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("test", "pass"));
        UserData user = dataAccess.getUser("test");
        assertNotNull(user);
        assertEquals(user.username(), "test");
        assertNotNull(user.password());
    }

    @Test
    public void getUserFailure() throws DataAccessException {
        UserData retUser = dataAccess.getUser("fake");
        assertNull(retUser);
    }

    // AUTH TESTS
    @Test
    public void createAuth() throws DataAccessException {
        AuthData auth = new AuthData("auth12345", "testUser");
        dataAccess.createAuth(auth.authToken(), auth);
        AuthData retAuth = dataAccess.getAuth("auth12345");
        assertNotNull(retAuth);
    }

    @Test
    public void createAuthMultiple() throws DataAccessException {
        AuthData auth1 = new AuthData("auth12345", "testUser1");
        AuthData auth2 = new AuthData("auth12346", "testUser2");
        AuthData auth3 = new AuthData("auth12347", "testUser3");
        dataAccess.createAuth(auth1.authToken(), auth1);
        dataAccess.createAuth(auth2.authToken(), auth2);
        dataAccess.createAuth(auth3.authToken(), auth3);

        AuthData retAuth1 = dataAccess.getAuth("auth12345");
        AuthData retAuth2 = dataAccess.getAuth("auth12346");
        AuthData retAuth3 = dataAccess.getAuth("auth12347");

        assertEquals(retAuth1.authToken(), auth1.authToken());
        assertEquals(retAuth1.username(), auth1.username());

        assertEquals(retAuth2.authToken(), auth2.authToken());
        assertEquals(retAuth2.username(), auth2.username());

        assertEquals(retAuth3.authToken(), auth3.authToken());
        assertEquals(retAuth3.username(), auth3.username());
    }

    @Test
    public void createAuthFailure() throws DataAccessException {
        assertThrows(NullPointerException.class, () -> dataAccess.createAuth("hi", null));
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        AuthData auth1 = new AuthData("auth12345", "testUser1");
        dataAccess.createAuth(auth1.authToken(), auth1);
        AuthData retAuth = dataAccess.getAuth("auth12345");
        assertNotNull(retAuth);
    }

    @Test
    public void getAuthFailure() throws DataAccessException {
        AuthData retAuth = dataAccess.getAuth("auth12345");
        assertNull(retAuth);
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        //create new auth
        AuthData auth1 = new AuthData("auth12345", "testUser1");
        dataAccess.createAuth(auth1.authToken(), auth1);
        // make sure it worked
        assertNotNull(dataAccess.getAuth("auth12345"));
        // delete it
        dataAccess.deleteAuth("auth12345");
        assertNull(dataAccess.getAuth("auth12345"));
    }

    // GAME TESTS
    @Test
    public void createGameSuccess() throws DataAccessException {
        // Create new game
        GameData newGame = new GameData(100, null, null, "lotsOfFun", new ChessGame());
        dataAccess.createGame(newGame);
        // Check to make sure it worked
        GameData retGame = dataAccess.getGame(100);
        assertEquals("lotsOfFun", retGame.gameName());
    }

    @Test
    public void createGameFailure() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(new GameData(100, null, null, null, new ChessGame())));
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        GameData game1 = new GameData(100, null, null, "lotsOfFun", new ChessGame());
        GameData game2 = new GameData(101, null, null, "cool", new ChessGame());
        GameData game3 = new GameData(102, null, null, "test", new ChessGame());

        dataAccess.createGame(game1);
        dataAccess.createGame(game2);
        dataAccess.createGame(game3);

        GameData retGame1 = dataAccess.getGame(100);
        GameData retGame2 = dataAccess.getGame(101);
        GameData retGame3 = dataAccess.getGame(102);

        assertEquals(retGame1.gameName(), game1.gameName());
        assertEquals(retGame2.gameName(), game2.gameName());
        assertEquals(retGame3.gameName(), game3.gameName());
    }

    @Test
    public void getGameFailure() throws DataAccessException {
        assertNull(dataAccess.getGame(101));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        GameData game1 = new GameData(100, null, null, "lotsOfFun", new ChessGame());
        GameData game2 = new GameData(101, null, null, "cool", new ChessGame());
        GameData game3 = new GameData(102, null, null, "test", new ChessGame());
        ArrayList<GameData> games = new ArrayList<>();
        games.add(game1);
        games.add(game2);
        games.add(game3);

        dataAccess.createGame(game1);
        dataAccess.createGame(game2);
        dataAccess.createGame(game3);

        ArrayList<GameData> retGames = (ArrayList<GameData>) dataAccess.listGames();
        assertEquals(retGames.size(), games.size());
        assertEquals(games, retGames);
    }

    @Test
    public void listGamesNoGames() throws DataAccessException {
        ArrayList<GameData> retGames = (ArrayList<GameData>) dataAccess.listGames();

        assertEquals(retGames.size(), 0);
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        ChessGame chess = new ChessGame();
        GameData game1 = new GameData(100, null, null, "lotsOfFun", chess);
        dataAccess.createGame(game1);
        assertEquals("lotsOfFun", dataAccess.getGame(100).gameName());

        GameData updated = new GameData(100, "testUser", "test2", "lotsOfFun", chess);
        dataAccess.update(updated);
        assertEquals("testUser", dataAccess.getGame(100).whiteUsername());
        assertEquals("test2", dataAccess.getGame(100).blackUsername());
    }

}

