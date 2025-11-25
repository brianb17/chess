package client;

import datamodel.AuthData;
import datamodel.ListGamesResult;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;
    private String authToken;
    private int port;

    @BeforeAll
    public void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @BeforeEach
    void setupFacade() {
        facade = new ServerFacade(port); // or store port in a variable if needed
    }

    @Test
    @DisplayName("Register user successfully")
    void testRegisterSuccess() throws Exception {
        AuthData auth = facade.register("sillygoose", "yourmom123", "goose@example.com");
        assertNotNull(auth);
        assertEquals("sillygoose", auth.username());
        authToken = auth.authToken();
    }

    @Test
    @DisplayName("Register user with missing info should fail")
    void testRegisterFail() {
        Exception ex = assertThrows(Exception.class, () -> {
            facade.register("fuzzycat", "", "cat@example.com");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("invalid request"));
    }

    @Test
    @DisplayName("Login successfully")
    void testLoginSuccess() throws Exception {
        AuthData auth = facade.login("sillygoose", "yourmom123");
        assertNotNull(auth);
        assertEquals("sillygoose", auth.username());
        authToken = auth.authToken();
    }

    @Test
    @DisplayName("Login with wrong password should fail")
    void testLoginFail() {
        Exception ex = assertThrows(Exception.class, () -> {
            facade.login("sillygoose", "wrongpass");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized") ||
                ex.getMessage().toLowerCase().contains("error"));
    }

    @Test
    @DisplayName("Create game successfully")
    void testCreateGameSuccess() throws Exception {
        int gameId = facade.createGame(authToken, "EpicBattleOfSocks");
        assertTrue(gameId > 0);
    }

    @Test
    @DisplayName("Create game with blank name should fail")
    void testCreateGameFail() {
        Exception ex = assertThrows(Exception.class, () -> {
            facade.createGame(authToken, "");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("bad request"));
    }

    @Test
    @DisplayName("List games successfully")
    void testListGames() throws Exception {
        ListGamesResult result = facade.listGames(authToken);
        assertNotNull(result);
        assertTrue(result.games().size() >= 0);
    }

    @Test
    @DisplayName("Join game successfully")
    void testJoinGame() throws Exception {
        int gameId = facade.createGame(authToken, "JoinableGame");
        facade.joinGame(authToken, gameId, "WHITE");
    }

    @Test
    @DisplayName("Join game with invalid color should fail")
    void testJoinGameFailColor() {
        Exception ex = assertThrows(Exception.class, () -> {
            facade.joinGame(authToken, 1, "GREEN");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("bad request"));
    }

    @Test
    @DisplayName("Logout successfully")
    void testLogout() throws Exception {
        facade.logout(authToken);
    }

    @Test
    @DisplayName("Logout with invalid token should fail gracefully")
    void testLogoutFail() {
        Exception ex = assertThrows(Exception.class, () -> {
            facade.logout("invalidToken");
        });
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized") ||
                ex.getMessage().toLowerCase().contains("error"));
    }

}
