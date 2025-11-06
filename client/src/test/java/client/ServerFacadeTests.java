package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.*;
import client.ServerFacade;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static UserData existingUser;
    private static AuthData existingAuthData;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws IOException {
        serverFacade.clear();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerSuccess() throws IOException {
        var auth = serverFacade.register("user", "password");

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
    }

    @Test
    public void registerFailure() throws IOException {
        var auth = serverFacade.register("user", "password");
        assertNotNull(auth);
        // should throw when registering same user
        assertThrows(IOException.class, () -> serverFacade.register("user", "wrong password"));
    }

    @Test
    public void loginSuccess() throws IOException {
        serverFacade.register("test", "poodlelover");
        var auth = serverFacade.login("test", "poodlelover");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
    }

    @Test
    public void loginFailure() throws IOException {
        assertThrows(IOException.class, () -> serverFacade.login("unregisteredUser", "pass"));
    }

    @Test
    public void logoutSuccess() throws IOException {
        serverFacade.register("loggingIn", "password");
        var auth = serverFacade.login("loggingIn", "password");

        serverFacade.logout(auth.authToken());
        // should not work the second time, already logged out
        assertThrows(IOException.class, () -> serverFacade.logout(auth.authToken()));
    }

    @Test
    public void logoutFailure() throws IOException {
        // logout a user with an invalid authToken
        assertThrows(IOException.class, () -> serverFacade.logout("randomAuth1234"));
    }

    @Test
    public void createGameSuccess() throws IOException {
        var auth = serverFacade.register("user", "password");
        var game = serverFacade.createGame("funGame", auth.authToken());
        assertNotNull(game);
        // check that the real game ID was updated
        assertNotEquals(0, game.gameID());
        // check that it came back with the same game name
        assertEquals("funGame", game.gameName());
    }

    @Test
    public void createGameFailure() throws IOException {
        // try to create a game with an invalid authToken
        assertThrows(IOException.class, () -> serverFacade.createGame("bruh", "fakeAuthToken"));
    }

    @Test
    public void listGamesSuccess() throws IOException {
        var auth = serverFacade.register("user", "password");
        serverFacade.createGame("funGame1", auth.authToken());
        serverFacade.createGame("funGame2", auth.authToken());
        serverFacade.createGame("funGame3", auth.authToken());

        var games = serverFacade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(3, games.size());
        assertEquals("funGame1", games.get(0).gameName());
        assertEquals("funGame2", games.get(1).gameName());
        assertEquals("funGame3", games.get(2).gameName());
    }

    @Test
    public void listGamesEmptyList() throws IOException {
        // empty games list works
        var auth = serverFacade.register("user", "password");
        var games = serverFacade.listGames(auth.authToken());
        assertEquals(0, games.size());
    }

    @Test
    public void listGamesFailure() throws IOException {
        // listing games with a fake authToken
        assertThrows(IOException.class, () -> serverFacade.listGames("fakeAuthToken"));
    }

    @Test
    public void joinGameBothPlayers() throws IOException {
        // start a game
        var auth = serverFacade.register("user", "password");
        var game = serverFacade.createGame("funGame1", auth.authToken());
        var games = serverFacade.listGames(auth.authToken());
        // verify that the whiteUsername starts as null
        assertEquals(games.get(0).whiteUsername(), null);

        // join as white player
        serverFacade.joinGame(game.gameID(), "WHITE", auth.authToken());

        // get games list again and confirm changes
        games = serverFacade.listGames(auth.authToken());
        assertEquals(games.get(0).whiteUsername(), "user");

        // join as black player
        var auth2 = serverFacade.register("blackPlayer", "password");
        serverFacade.joinGame(game.gameID(), "BLACK", auth2.authToken());
        games = serverFacade.listGames(auth.authToken());
        assertEquals(games.get(0).blackUsername(), "blackPlayer");
    }

    @Test
    public void joinColorAlreadyTaken() throws IOException {
        var auth = serverFacade.register("user", "password");
        var game = serverFacade.createGame("funGame1", auth.authToken());
        serverFacade.joinGame(game.gameID(), "WHITE", auth.authToken());

        var auth2 = serverFacade.register("blackPlayer", "password");
        assertThrows(IOException.class, () -> serverFacade.joinGame(game.gameID(), "WHITE", auth2.authToken()));
    }
}
