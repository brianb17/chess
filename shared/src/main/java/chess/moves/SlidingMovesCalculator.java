package chess.moves;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SlidingMovesCalculator {

    public static Collection<ChessMove> calculate(
            ChessBoard board, ChessPosition start, ChessPiece piece, int[][] directions) {

        List<ChessMove> moves = new ArrayList<>();

        for (int[] dir : directions) {
            int row = start.getRow();
            int col = start.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) break;

                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(newPos);

                if (target == null) {
                    moves.add(new ChessMove(start, newPos, null));
                } else {
                    if (target.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(start, newPos, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
