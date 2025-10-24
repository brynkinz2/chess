package dataaccess;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private Map<String, UserData> users = new HashMap<>();
    private Map<String, AuthData> authTokens = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        authTokens.clear();
        games.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        else {
            users.put(user.username(), user);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void createAuth(String authToken, AuthData authData) throws DataAccessException {
        authTokens.put(authToken, authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(String gameName) throws DataAccessException {
        return games.get(gameName);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void update(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game does not exist");
        }
        games.put(game.gameID(), game);
    }
}
