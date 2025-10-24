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

    public RegisterResult register(String testUser, String password) throws DataAccessException {
        if (testUser == null || password == null) {
            throw new DataAccessException("Username and password cannot be null");
        }
        UserData user = new UserData(testUser, password);
        dataAccess.createUser(user);

        String authToken = generateAuthToken();
        AuthData userAuth = new AuthData(authToken, user.username());
        dataAccess.createAuth(authToken, userAuth);
        return new RegisterResult(testUser, authToken);
    }



    private String generateAuthToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public record RegisterResult(String username, String authToken) {}
    public record LoginResult(String username, String authToken) {}
}
