package chess;

import chess.moves.JumpMovesCalculator;
import chess.moves.PawnMovesCalculator;
import chess.moves.SlidingMovesCalculator;

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

        return switch (piece.getPieceType()) {
            case BISHOP -> SlidingMovesCalculator.calculate(board, myPosition, piece, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            });
            case ROOK -> SlidingMovesCalculator.calculate(board, myPosition, piece, new int[][]{
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case QUEEN -> SlidingMovesCalculator.calculate(board, myPosition, piece, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case KNIGHT -> JumpMovesCalculator.calculate(board, myPosition, piece, new int[][]{
                    {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
            });
            case KING -> JumpMovesCalculator.calculate(board, myPosition, piece, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case PAWN -> PawnMovesCalculator.calculate(board, myPosition, piece);
            default -> List.of();
        };
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
