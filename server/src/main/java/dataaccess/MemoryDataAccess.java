package dataaccess;

import datamodel.UserData;
import datamodel.AuthData;
import datamodel.GameData;

import java.util.HashMap;
import java.util.HashSet;

public class MemoryDataAccess implements DataAccess{
    private final HashMap<String, UserData> users = new HashMap<>();
    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
