package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(String username, String password) throws DataAccessException {
        if (username == null || password == null) {
            throw new DataAccessException("Error: bad request");
        }
        UserData user = dataAccess.getUser(username);
        if (user != null) {
            throw new DataAccessException("Error: already taken");
        }
        user = new UserData(username, password);
        dataAccess.createUser(user);

        String authToken = generateAuthToken();
        AuthData userAuth = new AuthData(authToken, user.username());
        dataAccess.createAuth(authToken, userAuth);
        return new RegisterResult(username, authToken);
    }

    public RegisterResult login(String username, String password) throws DataAccessException {
        if (username == null || password == null) {
            throw new DataAccessException("bad request");
        }
        UserData user = dataAccess.getUser(username);
        if (user == null) {
            throw new DataAccessException("unauthorized");
        }
        // Check if password is hashed (starts with $2a$ for BCrypt)
        boolean isValid;
        if (user.password().startsWith("$2a$")) {
            // Database password - verify with BCrypt
            isValid = BCrypt.checkpw(password, user.password());
        } else {
            // Memory password - plain text comparison (for backward compatibility)
            isValid = password.equals(user.password());
        }

        if (!isValid) {
            throw new DataAccessException("unauthorized: Invalid password");
        }
        String authToken = generateAuthToken();
        AuthData userAuth = new AuthData(authToken, user.username());
        dataAccess.createAuth(authToken, userAuth);
        return new RegisterResult(user.username(), authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }



    private String generateAuthToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public record RegisterResult(String username, String authToken) {}
    public record LoginResult(String username, String authToken) {}
}
