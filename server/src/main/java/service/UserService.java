package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(String username, String password) throws DataAccessException {
        if (username == null || password == null) {
            throw new DataAccessException("Username and password cannot be null");
        }
        UserData user = new UserData(username, password);
        dataAccess.createUser(user);

        String authToken = generateAuthToken();
        AuthData userAuth = new AuthData(authToken, user.username());
        dataAccess.createAuth(authToken, userAuth);
        return new RegisterResult(username, authToken);
    }

    public RegisterResult login(String username, String password) throws DataAccessException {
        if (username == null || password == null) {
            throw new DataAccessException("Username and password cannot be null");
        }
        UserData user = dataAccess.getUser(username);
        if (user == null) {
            throw new DataAccessException("User not found");
        }
        if (!user.password().equals(password)) {
            throw new DataAccessException("Username and password do not match");
        }
        String authToken = generateAuthToken();
        AuthData userAuth = new AuthData(authToken, user.username());
        dataAccess.createAuth(authToken, userAuth);
        return new RegisterResult(user.username(), authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("User not logged in");
        }
        dataAccess.deleteAuth(authToken);
    }



    private String generateAuthToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public record RegisterResult(String username, String authToken) {}
    public record LoginResult(String username, String authToken) {}
}
