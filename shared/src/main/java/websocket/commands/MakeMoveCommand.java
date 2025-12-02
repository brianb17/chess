package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand  extends UserGameCommand{
    private final ChessMove move;

    public MakeMoveCommand() {
        super(CommandType.MAKE_MOVE, null, null);
        this.move = null;
    }

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
