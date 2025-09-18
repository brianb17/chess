package chess.moves;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator {

    public static Collection<ChessMove> calculate(
            ChessBoard board, ChessPosition start, ChessPiece piece) {
        List<ChessMove> moves = new ArrayList<>();

        int startRow = start.getRow();
        int startCol = start.getColumn();
        int direction;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            direction = 1;
        }
        else {
            direction = -1;
        }
        int frontRow = startRow + direction;
        if (frontRow >= 1 && frontRow <= 8) {
            ChessPosition forwardPos = new ChessPosition(frontRow, startCol);
            if (board.getPiece(forwardPos) == null) {
                addPawnMove(moves, start, forwardPos, piece);

                int sRank;
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    sRank = 2;
                }
                else {
                    sRank = 7;
                }
                if (startRow == sRank) {
                    int doubleRow = startRow + 2 * direction;
                    ChessPosition doublePos = new ChessPosition(doubleRow, startCol);
                    if (board.getPiece(doublePos) == null) {
                        moves.add(new ChessMove(start, doublePos, null));
                    }
                }
            }
        }

        int[][] captures = { {direction, -1}, {direction, 1} };
        for (int[] cap : captures) {
            int newRow = startRow + cap[0];
            int newCol = startCol + cap[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition capturePos = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(capturePos);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    addPawnMove(moves, start, capturePos, piece);
                }
            }
        }
        return moves;
    }

    private static void addPawnMove(List<ChessMove> moves, ChessPosition start, ChessPosition end, ChessPiece piece) {
        int promoRow;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            promoRow = 8;
        }
        else {
            promoRow = 1;
        }
        if (end.getRow() == promoRow) {
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        }
        else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
