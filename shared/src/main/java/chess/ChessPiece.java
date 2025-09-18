package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            return slidingMoves(board, myPosition, piece, new int[][] {
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
            });
        }
        return List.of();
    }

    private Collection<ChessMove> slidingMoves(
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
                }
                else {
                    if (target.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(start, newPos, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
