package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import datamodel.UserData;
import datamodel.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {

    private ClearService clearService;
    private MemoryDataAccess dataAccess;
    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);

        UserData user = new UserData("bob", "bob@example.com", "password");
        AuthData auth = userService.register(user);
    }

    @Test
    void clearTestPositive() throws DataAccessException {
        assertFalse(dataAccess.listGames().size() > 0 && dataAccess.getUser("bob") == null);
        clearService.clearApp();
        assertNull(dataAccess.getUser("bob"));
        assertEquals(0, dataAccess.listGames().size());
        assertEquals(0, dataAccess.getAllGames().size());
    }

    @Test
    void clearTestNegative() throws DataAccessException {
        Assertions.assertDoesNotThrow(() -> clearService.clearApp());
    }
}
