package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.AuthData;
import datamodel.ListGamesResult;
import datamodel.JoinGameRequest;
import chess.ChessGame;

import javax.naming.ServiceUnavailableException;
import javax.xml.crypto.Data;
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

    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {

            throw new IllegalArgumentException("Error: unauthorized");
        }

        String color = request.playerColor();
        if (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color)) {
            throw new IllegalArgumentException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new IllegalArgumentException("Error: bad request");

        }

        if ("WHITE".equalsIgnoreCase(color) && game.whiteUsername() != null) {
            throw new IllegalStateException("Error: already taken");
        }

        if ("BLACK".equalsIgnoreCase(color) && game.blackUsername() != null) {
            throw new IllegalStateException("Error: already taken");
        }

        if ("WHITE".equalsIgnoreCase(color)) {
            game = new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
        }
        else {
            game = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game()
            );
        }

        dataAccess.updateGame(game);
    }
}
