package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
// Need tables for:
// User: Username, password, Email?
// Auth: AuthToken, Username
// Game: GameID, WhiteUser, BlackUser, GameName

public class MySQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    public void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try(var conn = DatabaseManager.getConnection()) {
            //Create User table
            String createUserTable = """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL
                )
                """;
            //Create Auth table
            String createAuthTable = """
                    CREATE TABLE IF NOT EXISTS auth (
                        authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                        username VARCHAR(255) NOT NULL
                    )
                """;
            //Create Game table
            String createGameTable = """
                    CREATE TABLE IF NOT EXISTS game (
                        gameID INTEGER NOT NULL PRIMARY KEY,
                        whiteUser VARCHAR(255),
                        blackUser VARCHAR(255),
                        gameName VARCHAR(255) NOT NULL,
                        game TEXT NOT NULL
                    )
                 """;
            try (var statement = conn.prepareStatement(createUserTable)) {
                statement.executeUpdate();
            }
            try (var statement = conn.prepareStatement(createAuthTable)) {
                statement.executeUpdate();
            }
            try (var statement = conn.prepareStatement(createGameTable)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE TABLE user")) {
                statement.executeUpdate();
            }
            try (var statement = conn.prepareStatement("TRUNCATE TABLE auth")) {
                statement.executeUpdate();
            }
            try (var statement = conn.prepareStatement("TRUNCATE TABLE game")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing database: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO user (username, password) VALUES (?, ?)";
        var encrypted = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        try(var conn = DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setString(1, user.username());
                stmt.setString(2, encrypted);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error code
                throw new DataAccessException("User already exists");
            }
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password FROM user WHERE username = ?";
        try(var conn = DatabaseManager.getConnection();
            var stmt = conn.prepareStatement(statement)) {
            stmt.setString(1, username);

            try (var result = stmt.executeQuery()) {
                if (result.next()) {
                    String user = result.getString("username");
                    String password = result.getString("password");
                    if (user == null || password == null) {
                        throw new DataAccessException("User not found");
                    }
                    return new UserData(user, password);
                }

            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createAuth(String authToken, AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn =DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setString(1, authToken);
                stmt.setString(2, authData.username());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setString(1, authToken);

                try (var result = stmt.executeQuery()) {
                    if (result.next()) {
                        String foundAuth = result.getString("authToken");
                        String username = result.getString("username");
                        return new AuthData(foundAuth, username);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting auth: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken = ?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setString(1, authToken);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth: " + e.getMessage());
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO game (gameID, whiteUser, blackUser, gameName, game) VALUES (?, ?, ?, ?, ?)";
        String gameJson = gson.toJson(game.game());
        try (var conn =DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setInt(1, game.gameID());
                stmt.setString(2, game.whiteUsername());
                stmt.setString(3, game.blackUsername());
                stmt.setString(4, game.gameName());
                stmt.setString(5, gameJson);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUser, blackUser, gameName, game FROM game WHERE gameID = ?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setInt(1, gameID);

                try (var result = stmt.executeQuery()) {
                    if (result.next()) {
                        int foundGameID = result.getInt("gameID");
                        String whiteUsername = result.getString("whiteUser");
                        String blackUsername = result.getString("blackUser");
                        String gameName = result.getString("gameName");
                        String gameJSON = result.getString("game");

                        ChessGame game = gson.fromJson(gameJSON, ChessGame.class);

                        return new GameData(foundGameID, whiteUsername, blackUsername, gameName, game);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        var res = new ArrayList<GameData>();
        var statement = "SELECT gameID, whiteUser, blackUser, gameName, game FROM game";

        try (var conn = DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                try (var result = stmt.executeQuery()) {
                    while (result.next()) {
                        int gameID = result.getInt("gameID");
                        String whiteUsername = result.getString("whiteUser");
                        String blackUsername = result.getString("blackUser");
                        String gameName = result.getString("gameName");
                        String gameJson = result.getString("game");

                        ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);

                        res.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return res;
    }

    @Override
    public void update(GameData game) throws DataAccessException {
        var statement = "UPDATE game SET whiteUser = ?, blackUser = ?, game = ? WHERE gameID = ?";
        String gameJson = gson.toJson(game.game());
        try (var conn =DatabaseManager.getConnection()) {
            try(var stmt = conn.prepareStatement(statement)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, gameJson);
                stmt.setInt(4, game.gameID());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }
}
