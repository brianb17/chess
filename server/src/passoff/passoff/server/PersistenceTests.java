package passoff.server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceTests {

    private DataAccess dataAccess;

    @BeforeEach
    void setup()  {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }


    //User Tests
    @Test
    void createUserSuccess() {
        var user = new UserData("brian", "b@test.com", "password123");
        dataAccess.createUser(user);

        var found = dataAccess.getUser("brian");
        assertNotNull(found);
        assertEquals("brian", found.username());
        assertEquals("b@test.com", found.email());
    }

    @Test
    void createUserDuplicateUsernameFail() {
        var user1 = new UserData("brian", "test@test.com", "password");
        var user2 = new UserData("brian", "test2@test.com", "password2");
        dataAccess.createUser(user1);
        assertThrows(RuntimeException.class, () -> dataAccess.createUser(user2));
    }

    @Test
    void getUserNotFoundReturnNull() {
        var result = dataAccess.getUser("nobody");
        assertNull(result);
    }

    @Test
    void getUserSuccessReturnsUser() {
        MySqlDataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        UserData user = new UserData("toastMaster", "CoolToast@toast.com", "NoButta");
        dataAccess.createUser(user);

        UserData retrieved = dataAccess.getUser("toastMaster");

        assertNotNull(retrieved);
        assertEquals("toastMaster", retrieved.username());
        assertEquals("CoolToast@toast.com", retrieved.email());
        assertEquals("NoButta", retrieved.password());
    }



    //Auth Tests
    @Test
    void createAuth_positive_createsAuthSuccessfully() {
        MySqlDataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        AuthData auth = new AuthData("milk", "token123");
        dataAccess.createAuth(auth);

        AuthData retrieved = dataAccess.getAuth("token123");
        assertNotNull(retrieved);
        assertEquals("milk", retrieved.username());
        assertEquals("token123", retrieved.authToken());
    }


    @Test
    void createAuth_negative_duplicateToken_throwsException() {
        MySqlDataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        AuthData auth1 = new AuthData("bread", "token456");
        dataAccess.createAuth(auth1);

        AuthData auth2 = new AuthData("butter", "token456");

        assertThrows(RuntimeException.class, () -> dataAccess.createAuth(auth2));
    }

    @Test
    void getAuth_positive_returnsAuth() {
        MySqlDataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        AuthData auth = new AuthData("toast", "token789");
        dataAccess.createAuth(auth);

        AuthData retrieved = dataAccess.getAuth("token789");
        assertNotNull(retrieved);
        assertEquals("toast", retrieved.username());
        assertEquals("token789", retrieved.authToken());
    }

    @Test
    void getAuth_negative_nonexistentToken_returnsNull() {
        MySqlDataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        AuthData retrieved = dataAccess.getAuth("ghostToken");
        assertNull(retrieved);
    }

    @Test
    void deleteAuth_negative_nonexistentToken_throwsException() {
        MySqlDataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        assertThrows(RuntimeException.class, () -> dataAccess.deleteAuth("missingToken"));
    }


    @Test
    void deleteAuthSuccess() {
        var auth = new AuthData("brian", "yourmom");
        dataAccess.createAuth(auth);
        dataAccess.deleteAuth("yourmom");
        var found = dataAccess.getAuth("yourmom");
        assertNull(found);
    }



    // Game Tests
    @Test
    void createGameSuccess() throws Exception {
        var dataAccess = new MySqlDataAccess();
        var game = new GameData(1, "yourmom", "yourdad", "EpicShowdown", new ChessGame());
        dataAccess.createGame(game);
        var returned = dataAccess.getGame(1);
        assertNotNull(returned);
        assertEquals("yourmom", returned.whiteUsername());
        assertEquals("yourdad", returned.blackUsername());
        assertEquals("EpicShowdown", returned.gameName());
    }

    @Test
    void createGameFailDuplicateID() throws Exception {
        var dataAccess = new MySqlDataAccess();
        var game1 = new GameData(5, "bigchungus", "slayqueen", "BattleOne", new ChessGame());
        var game2 = new GameData(5, "spicyboy", "coolguy", "BattleTwo", new ChessGame());
        dataAccess.createGame(game1);
        assertThrows(RuntimeException.class, () -> dataAccess.createGame(game2));
    }


    @Test
    void createAndGetGame() {
        dataAccess.createUser(new UserData("Alice", "alice@test.com", "pass"));
        dataAccess.createUser(new UserData("Bob", "bob@test.com", "pass"));

        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "Alice", "Bob", "Epic Match", chessGame);
        dataAccess.createGame(game);

        GameData retrieved = dataAccess.getGame(1);
        assertNotNull(retrieved);
        assertEquals("Epic Match", retrieved.gameName());
        assertEquals("Alice", retrieved.whiteUsername());
        assertEquals("Bob", retrieved.blackUsername());

        // Compare game states via JSON
        Gson gson = new Gson();
        assertEquals(gson.toJson(chessGame), gson.toJson(retrieved.game()));
    }

    @Test
    void getGameNegativeReturnsNull() {
        MySqlDataAccess dao = new MySqlDataAccess();
        dao.clear();

        GameData retrieved = dao.getGame(999);
        assertNull(retrieved);
    }

    @Test
    void getAllGamesReturnsCorrectMap() {
        ChessGame game1Chess = new ChessGame();
        ChessGame game2Chess = new ChessGame();
        GameData game1 = new GameData(1, "Alice", "Bob", "Match 1", game1Chess);
        GameData game2 = new GameData(2, "Charlie", "Dana", "Match 2", game2Chess);

        dataAccess.createGame(game1);
        dataAccess.createGame(game2);

        HashMap<Integer, GameData> allGames = dataAccess.getAllGames();
        assertEquals(2, allGames.size());
        assertTrue(allGames.containsKey(1));
        assertTrue(allGames.containsKey(2));
    }

    @Test
    void getAllGamesNegative() {
        MySqlDataAccess dao = new MySqlDataAccess();
        dao.clear();

        HashMap<Integer, GameData> allGames = dao.getAllGames();
        assertNotNull(allGames);
        assertTrue(allGames.isEmpty());
    }

    @Test
    void listGamesReturnsAllGames() {
        ChessGame game1Chess = new ChessGame();
        ChessGame game2Chess = new ChessGame();
        GameData game1 = new GameData(1, "Alice", "Bob", "Match 1", game1Chess);
        GameData game2 = new GameData(2, "Charlie", "Dana", "Match 2", game2Chess);

        dataAccess.createGame(game1);
        dataAccess.createGame(game2);

        List<GameData> games = dataAccess.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGamesNegative() {
        MySqlDataAccess dao = new MySqlDataAccess();
        dao.clear();

        List<GameData> games = dao.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGameSuccess() throws DataAccessException, InvalidMoveException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "Alice", "Bob", "Match 1", chessGame);
        dataAccess.createGame(game);

        ChessMove move = new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4,5),
                null
        );
        chessGame.makeMove(move);
        GameData updatedGame = new GameData(1, "Alice", "Bob", "Match 1", chessGame);
        dataAccess.updateGame(updatedGame);

        GameData retrieved = dataAccess.getGame(1);
        Gson gson = new Gson();
        assertEquals(
                gson.toJson(chessGame),
                gson.toJson(retrieved.game()),
                "game updated"
        );
    }

    @Test
    void updateGameNegative() {
        MySqlDataAccess dao = new MySqlDataAccess();
        dao.clear();

        ChessGame fakeGame = new ChessGame();
        GameData game = new GameData(999, "milk", "bread", "FunnyGame", fakeGame);

        assertThrows(RuntimeException.class, () -> dao.updateGame(game));
    }

    @Test
    void clearTest() {
        MySqlDataAccess dao = new MySqlDataAccess();
        dao.clear();

        UserData user = new UserData("milk", "password123", "milk@example.com");
        dao.createUser(user);

        AuthData auth = new AuthData("milk", "token123");
        dao.createAuth(auth);

        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "milk", "bread", "FunnyGame", chessGame);
        dao.createGame(game);

        dao.clear();

        assertNull(dao.getUser("milk"));
        assertNull(dao.getAuth("token123"));
        assertTrue(dao.getAllGames().isEmpty());
    }


}
