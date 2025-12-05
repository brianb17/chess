package dataaccess;

import java.sql.*;
import java.util.*;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.GameService;

public class MySqlDataAccess implements DataAccess {

    private static final String CREATE_AUTH_TABLE = """
        CREATE TABLE IF NOT EXISTS auth (
            username VARCHAR(50) NOT NULL,
            token VARCHAR(255) NOT NULL,
            PRIMARY KEY (token)
        );
        """;

    private static final String CREATE_USER_TABLE = """
        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(50) NOT NULL PRIMARY KEY,
            password VARCHAR(60) NOT NULL,
            email VARCHAR(100) NOT NULL UNIQUE
        );
    """;

    private static final String CREATE_GAME_TABLE = """
        CREATE TABLE IF NOT EXISTS game (
            gameID INT NOT NULL AUTO_INCREMENT,
                        whiteUsername VARCHAR(255),
                        blackUsername VARCHAR(255),
                        gameName VARCHAR(255),
                        chessGame LONGTEXT NOT NULL,
                        PRIMARY KEY (gameID)
        );
    """;

    private final Gson gson = new Gson();

    public MySqlDataAccess() {
        try {
            DatabaseManager.createDatabase(); // ensure database exists first
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to create database", e);
        }
        createTables(); // then create tables
    }

    private void createTables() {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(CREATE_USER_TABLE);
            stmt.execute(CREATE_GAME_TABLE);
            stmt.execute(CREATE_AUTH_TABLE);
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
            e.printStackTrace();
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
            int deletedRows = stmt.executeUpdate();
            if (deletedRows == 0) {
                throw new RuntimeException("Authtoken doesn't exist.");
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to delete auth", e);
        }
    }

    @Override
    public void createGame(GameData game) {
        try (var conn = DatabaseManager.getConnection()) {

            // Generate gameID
            int gameID;
            try (var stmt = conn.prepareStatement("SELECT COALESCE(MAX(gameID), 0) + 1 FROM game");
                 var rs = stmt.executeQuery()) {
                rs.next();
                gameID = rs.getInt(1);
            }

            // Insert new game
            String sql = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?, ?)";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, gameID);
                stmt.setString(2, game.whiteUsername());
                stmt.setString(3, game.blackUsername());
                stmt.setString(4, game.gameName());
                stmt.setString(5, gson.toJson(game.game()));

                stmt.executeUpdate();
            }

        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to create game", e);
        }
    }



    @Override
    public GameData getGame(int gameID) {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ChessGame chessGame = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            chessGame
                    );
                }
            }
            return null;

        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to get game", e);
        }
    }

    @Override
    public HashMap<Integer, GameData> getAllGames() {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game";
        HashMap<Integer, GameData> allGames = new HashMap<>();
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("gameID");
                ChessGame chessGame = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                GameData game = new GameData(
                        id,
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                );
                allGames.put(id, game);
            }

            return allGames;

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to get all games", e);
        }
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(getAllGames().values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET gameName = ?, whiteUsername = ?, blackUsername = ?, chessGame = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.gameName());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, gson.toJson(game.game())); // serialize ChessGame
            stmt.setInt(5, game.gameID());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("Game with ID " + game.gameID() + " does not exist.");
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to update game", e);
        }
    }

}
