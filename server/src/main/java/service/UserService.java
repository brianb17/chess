package service;

import dataaccess.DataAccess;
import datamodel.AuthData;
import datamodel.UserData;

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
        dataAccess.createUser(user);

        String token = generateAuthToken();
        var authData = new AuthData(user.username(), token);
        dataAccess.createAuth(authData);

        return authData;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
