package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private DataAccess dataAccess;
    private UserService userService;


    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess =  new MemoryDataAccess();
        userService = new UserService(dataAccess);
        dataAccess.clear();
    }

    // REGISTER TESTS
    @Test
    public void normalRegister() throws DataAccessException {
        String username = "test";
        String password = "test1234";
        var result = userService.register(username, password);

        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(result.authToken());

        UserData user = dataAccess.getUser(username);
        assertNotNull(user);
        assertEquals(username, user.username());
        assertEquals(password, user.password());
    }

    @Test
    public void registerMissingFields() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> userService.register(null, "pass1234"));
        assertThrows(DataAccessException.class, () -> userService.register("user", null));
    }

    @Test
    public void registerExistingUser() throws DataAccessException {
        userService.register("ExistingUser", "passcode");

       assertThrows(DataAccessException.class, () -> userService.register("ExistingUser", "passcode"));
    }

    // LOGIN TESTS

    @Test
    public void loginExistingUser() throws DataAccessException {
        String username = "ExistingUser";
        String password = "passcode";
        userService.register(username, password);

        var result = userService.login(username, password);
        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginInvalidPassword() throws DataAccessException {
        String username = "ExistingUser";
        String password = "passcode";
        userService.register(username, password);

        assertThrows(DataAccessException.class, () -> userService.login(username, "invalid"));
    }

    @Test
    public void loginUsername() throws DataAccessException {
        String username = "ExistingUser";
        String password = "passcode";

        assertThrows(DataAccessException.class, () -> userService.login(username, password));
    }
}
