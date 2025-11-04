package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.*;
import client.ServerFacade;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

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

}
