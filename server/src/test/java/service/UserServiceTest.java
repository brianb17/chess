package service;

import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private MemoryDataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
    }

    //REGISTER TESTS

    @Test
    void registerUserSuccess() throws Exception {
        UserData user = new UserData("bob", "bob@example.com", "password123");
        AuthData auth = userService.register(user);

        assertNotNull(auth, "AuthData should not be null");
        assertEquals("bob", auth.username(), "Username should match registered user");
        assertNotNull(auth.authToken(), "Auth token should be generated");
        assertNotNull(dataAccess.getUser("bob"), "User should exist in data access");
    }

    @Test
    void registerExistingUserFail() throws Exception {
        UserData user = new UserData("alice", "alice@example.com", "pw");
        userService.register(user);
        UserData duplicate = new UserData("alice", "other@example.com", "pw2");
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            userService.register(duplicate);
        });
        assertEquals("already exists", exception.getMessage());
    }

    @Test
    void registerInvalidUserFails() {
        UserData invalid = new UserData("", "", "");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(invalid);
        });
        assertEquals("invalid request", exception.getMessage());
    }

    //LOGIN TESTS

    @Test
    void loginSuccess() throws Exception {
        UserData user = new UserData("charlie", "charlie@example.com", "pw123");
        userService.register(user);
        AuthData auth = userService.login("charlie", "pw123");

        assertNotNull(auth, "AuthData should not be null");
        assertEquals("charlie", auth.username(), "Username should match");
        assertNotNull(auth.authToken(), "Auth token should be generated");
    }

    @Test
    void loginWrongPassFails() throws Exception {
        UserData user = new UserData("dave", "dave@example.com", "pw123");
        userService.register(user);

        AuthData auth = userService.login("dave", "wrongpw");
        assertNull(auth, "AuthData should be null for wrong password");
    }

    @Test
    void loginBadUserFails() {
        AuthData auth = userService.login("nonexistent", "pw");
        assertNull(auth, "AuthData should be null for nonexistent user");
    }

    @Test
    void loginBadArgsFail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(null, "pw");
        });
        assertEquals("bad request", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login("username", null);
        });
        assertEquals("bad request", exception.getMessage());
    }

    //LOGOUT TESTS

    @Test
    void logoutSuccess() throws Exception {
        UserData user = new UserData("eve", "eve@example.com", "pw");
        AuthData auth = userService.register(user);

        boolean result = userService.logout(auth.authToken());
        assertTrue(result, "Logout should succeed");
        assertNull(dataAccess.getAuth(auth.authToken()), "Auth token should be removed from data access");
    }

    @Test
    void logoutBadTokenFail() {
        boolean result = userService.logout("invalidToken");
        assertFalse(result, "Logout should fail for invalid token");
    }

    @Test
    void logoutNullTokenFail() {
        boolean result = userService.logout(null);
        assertFalse(result, "Logout should fail for null token");
    }
}
