package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.AuthData;
import datamodel.ListGamesResult;
import chess.ChessGame;

import javax.naming.ServiceUnavailableException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException{
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("bad request");
        }

        Map<Integer, GameData> allGames = dataAccess.getAllGames();
        int gameID = allGames.size() + 1;

        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        dataAccess.createGame(game);
        return gameID;
    }

    public Object listGames(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        List<GameData> games;
        games = dataAccess.listGames();

        return new ListGamesResult(games);
    }
}
