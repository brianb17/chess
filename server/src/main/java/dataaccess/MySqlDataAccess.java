package dataaccess;

import java.sql.*;
import java.util.*;
import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.GameService;

public class MySqlDataAccess implements DataAccess {

    private static final String CREATE_USER_TABLE = """
        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(50) PRIMARY KEY,
            password VARCHAR(60) NOT NULL,
            email VARCHAR(100) NOT NULL UNIQUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        );
    """;

    private static final String CREATE_GAME_TABLE = """
        CREATE TABLE IF NOT EXISTS game (
            game_id INT AUTO_INCREMENT PRIMARY KEY,
            white_player VARCHAR(50),
            black_player VARCHAR(50),
            state TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            FOREIGN KEY (white_player) REFERENCES user(username),
            FOREIGN KEY (black_player) REFERENCES user(username)
        );
    """;

    // Optional: moves table
    private static final String CREATE_MOVE_TABLE = """
        CREATE TABLE IF NOT EXISTS move (
            move_id INT AUTO_INCREMENT PRIMARY KEY,
            game_id INT NOT NULL,
            move_number INT NOT NULL,
            move_data VARCHAR(10) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (game_id) REFERENCES game(game_id)
        );
    """;

    private final Gson gson = new Gson();

    public MySqlDataAccess() {
        // Initialize tables on startup
        createTables();
    }

    private void createTables() {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(CREATE_USER_TABLE);
            stmt.execute(CREATE_GAME_TABLE);
            stmt.execute(CREATE_MOVE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create database tables", e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // -------- UserDao Methods --------
    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM move");
            stmt.executeUpdate("DELETE FROM game");
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM user");

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to clear database", e);
        }
    }

    public void createUser(UserData user) {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) {
        String sql = "SELECT username, email, password FROM user WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
            }
            return null; // Same behavior as memory
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to get user", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) {
        String sql = "INSERT INTO auth (username, token) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auth.username());
            stmt.setString(2, auth.authToken());

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String token) {
        String sql = "SELECT username, token FROM auth WHERE token = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("username"),
                            rs.getString("token")
                    );
                }
            }
            return null;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String token) {
        String sql = "DELETE FROM auth WHERE token = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to delete auth", e);
        }
    }

    @Override
    public void createGame(GameData game) {

    }


    @Override
    public GameData getGame(int gameId) {
        // stub: implement later
        return null;
    }

    @Override
    public HashMap<Integer, GameData> getAllGames() {
        return null;
    }

    @Override
    public List<GameData> listGames() {
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

}
