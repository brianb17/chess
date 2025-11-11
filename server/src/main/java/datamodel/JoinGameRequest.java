package datamodel;

public record JoinGameRequest(
        int gameID,
        String playerColor // must match JSON exactly
) {}
