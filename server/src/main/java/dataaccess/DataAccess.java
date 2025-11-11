package dataaccess;

import datamodel.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public interface DataAccess {
    void clear();
    void createUser(UserData user);
    UserData getUser(String username);

    void createAuth(AuthData auth);
    AuthData getAuth(String token);
    void deleteAuth(String token);

    void createGame(GameData game);
    GameData getGame(int gameID);
    HashMap<Integer, GameData> getAllGames();

    List<GameData> listGames();
}
