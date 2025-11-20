package passoff.server;

import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import datamodel.AuthData;
import datamodel.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
