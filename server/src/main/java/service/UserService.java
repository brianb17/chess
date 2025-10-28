package service;

import dataaccess.DataAccess;
import datamodel.AuthData;
import datamodel.UserData;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("already exists");
        }
        dataAccess.createUser(user);
        var authData = new AuthData(user.username(), generateAuthToken());

        return authData;
    }

    private String generateAuthToken() {
        return "xyz";
    }
}
