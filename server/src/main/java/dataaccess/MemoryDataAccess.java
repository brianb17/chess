package dataaccess;

import datamodel.UserData;
import datamodel.AuthData;
import datamodel.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MemoryDataAccess implements DataAccess{
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> authTokens = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }
    public AuthData getAuth(String token) {
        return authTokens.get(token);
    }

    public void deleteAuth(String token) {
        authTokens.remove(token);
    }

    public void createGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public GameData getGame(int id) {
        return games.get(id);

    }

    public HashMap<Integer, GameData> getAllGames() {
        return games;
    }

    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }
}
