package chess.moves;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JumpMovesCalculator {

    public static Collection<ChessMove> calculate(
            ChessBoard board, ChessPosition start, ChessPiece piece, int[][] directions) {

        List<ChessMove> moves = new ArrayList<>();
        int startRow = start.getRow();
        int startCol = start.getColumn();

        for (int[] offset : directions) {
            int newRow = startRow + offset[0];
            int newCol = startCol + offset[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(newPos);

                if (target == null || target.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(start, newPos, null));
                }
            }
        }
        return moves;
    }
}
