package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.AuthData;
import io.javalin.http.UnauthorizedResponse;
import chess.ChessGame;

import javax.xml.crypto.Data;
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
            throw new DataAccessException("bad bad request");
        }

        Map<Integer, GameData> allGames = dataAccess.getAllGames();
        int gameID = allGames.size() + 1;

        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        dataAccess.createGame(game);
        return gameID;
    }
}
