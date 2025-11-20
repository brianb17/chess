package passoff.server;

import chess.ChessGame;
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

    @Test
    void createUserSucess() {
        var user = new UserData("brian", "b@test.com", "password123");
        dataAccess.createUser(user);

        var found = dataAccess.getUser("brian");
        assertNotNull(found);
        assertEquals("brian", found.username());
        assertEquals("b@test.com", found.email());
    }

    @Test
    void getUserNotFoundReturnNull() {
        var result = dataAccess.getUser("nobody");
        assertNull(result);
    }

    @Test
    void deleteAuthSuccess() {
        var auth = new AuthData("brian", "yourmom");
        dataAccess.createAuth(auth);
        dataAccess.deleteAuth("yourmom");
        var found = dataAccess.getAuth("yourmom");
        assertNull(found);
    }

    @Test
    void createUserDuplicateUsernameFail() {
        var user1 = new UserData("brian", "test@test.com", "password");
        var user2 = new UserData("brian", "test2@test.com", "password2");
        dataAccess.createUser(user1);
        assertThrows(RuntimeException.class, () -> dataAccess.createUser(user2));
    }

    @Test
    void createAndGetGame() {
        // First create users because game has foreign key to user.username
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
    void updateGameSuccess() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(1, "Alice", "Bob", "Match 1", chessGame);
        dataAccess.createGame(game);

        GameData updatedGame = new GameData(1, "Alice", "Bob", "Updated Match", chessGame);
        dataAccess.updateGame(updatedGame);

        GameData retrieved = dataAccess.getGame(1);
        assertEquals("Updated Match", retrieved.gameName());
    }


}
