package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.JoinGameRequest;
import datamodel.ListGamesResult;
import datamodel.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private MemoryDataAccess dataAccess;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        UserData user = new UserData("bob", "bob@example.com", "password");
        AuthData auth = userService.register(user);
        authToken = auth.authToken();
    }

    //CREATE GAME

    @Test
    void createGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(authToken, "Chess Match");
        assertEquals(1, gameID, "First game should have ID 1");
        assertNotNull(dataAccess.getGame(gameID));
    }

    @Test
    void createGameUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("invalidToken", "Chess Match");
        });
        assertEquals("unauthorized", exception.getMessage());
    }

    @Test
    void createGameInvalidName() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(authToken, "");
        });
        assertEquals("bad request", exception.getMessage());
    }

    //LIST GAMES

    @Test
    void listGamesSuccess() throws DataAccessException {
        gameService.createGame(authToken, "Game 1");
        gameService.createGame(authToken, "Game 2");

        ListGamesResult result = (ListGamesResult) gameService.listGames(authToken);
        assertEquals(2, result.games().size(), "Should return 2 games");
    }

    @Test
    void listGamesUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("invalidToken");
        });
        assertEquals("unauthorized", exception.getMessage());
    }

    //JOIN GAME

    @Test
    void joinGameSuccess() throws Exception {
        int gameID = gameService.createGame(authToken, "Game 1");

        UserData user2 = new UserData("alice", "alice@example.com", "pw");
        AuthData auth2 = userService.register(user2);

        JoinGameRequest whiteRequest = new JoinGameRequest(gameID, "WHITE");
        gameService.joinGame(authToken, whiteRequest);

        JoinGameRequest blackRequest = new JoinGameRequest(gameID, "BLACK");
        gameService.joinGame(auth2.authToken(), blackRequest);

        assertEquals("bob", dataAccess.getGame(gameID).whiteUsername());
        assertEquals("alice", dataAccess.getGame(gameID).blackUsername());
    }

    @Test
    void joinGameTakenFails() throws Exception {
        int gameID = gameService.createGame(authToken, "Game 1");
        JoinGameRequest whiteRequest = new JoinGameRequest(gameID, "WHITE");
        gameService.joinGame(authToken, whiteRequest);

        UserData user2 = new UserData("alice", "alice@example.com", "pw");
        AuthData auth2 = userService.register(user2);
        String aliceAuthToken = auth2.authToken();

        JoinGameRequest aliceJoinsWhite = new JoinGameRequest(gameID, "WHITE");

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gameService.joinGame(aliceAuthToken, aliceJoinsWhite);
        });
        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    void joinGameBadColorFail() throws Exception {
        int gameID = gameService.createGame(authToken, "Game 1");
        JoinGameRequest request = new JoinGameRequest(gameID, "GREEN");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(authToken, request);
        });
        assertTrue(exception.getMessage().contains("bad request"));
    }

    @Test
    void joinGameDoesNotExistFail() {
        JoinGameRequest request = new JoinGameRequest(999, "WHITE");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(authToken, request);
        });
        assertTrue(exception.getMessage().contains("bad request"));
    }

    @Test
    void joinGameUnauthorizedFail() throws Exception {
        int gameID = gameService.createGame(authToken, "Game 1");
        JoinGameRequest request = new JoinGameRequest(gameID, "WHITE");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame("badToken", request);
        });
        assertTrue(exception.getMessage().contains("unauthorized"));
    }
}
