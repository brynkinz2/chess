package dataaccess;

import model.*;
import org.eclipse.jetty.server.Authentication;

import java.util.List;

public interface DataAccess {

    // Clear all data
    void clear() throws DataAccessException;

    // User functions
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Auth functions
    void createAuth(String authToken, AuthData authData) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // Game operations
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void update(GameData game) throws DataAccessException;
}
