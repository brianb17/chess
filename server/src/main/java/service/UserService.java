package service;

import dataaccess.DataAccess;
import datamodel.AuthData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.username().isEmpty()
                || user.password() == null || user.password().isEmpty()
                || user.email() == null || user.email().isEmpty()) {
            throw new IllegalArgumentException("invalid request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("already exists");
        }

        String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(
                user.username(),
                user.email(),
                hashed
        );
        dataAccess.createUser(hashedUser);

        String token = generateAuthToken();
        var authData = new AuthData(user.username(), token);
        dataAccess.createAuth(authData);

        return authData;
    }

    public AuthData login(String username, String password) throws IllegalArgumentException {
        if (username == null || password == null) {
            throw new IllegalArgumentException("bad request");
        }

        UserData user = dataAccess.getUser(username);
        if (user == null) {
            return null;
        }
        if (!BCrypt.checkpw(password, user.password())) {
            return null;
        }
        String token = generateAuthToken();
        AuthData auth = new AuthData(user.username(), token);
        dataAccess.createAuth(auth);
        return auth;
    }

    public boolean logout(String authToken) {
        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            return false;
        }
        dataAccess.deleteAuth(authToken);
        return true;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
