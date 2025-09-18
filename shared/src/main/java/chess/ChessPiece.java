package chess;

import chess.moves.SlidingMovesCalculator;

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

        switch (piece.getPieceType()) {
            case BISHOP:
                return SlidingMovesCalculator.calculate(board, myPosition, piece, new int[][] {
                    {1,1}, {1,-1}, {-1,1}, {-1,-1}
                });
            case ROOK:
                return SlidingMovesCalculator.calculate(board, myPosition, piece, new int[][] {
                        {1,0}, {-1,0}, {0,1}, {0,-1}
                });
            case QUEEN:
                return SlidingMovesCalculator.calculate(board, myPosition, piece, new int[][] {
                        {1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}
                });
            default:
                return List.of();
        }
//        if (piece.getPieceType() == PieceType.BISHOP) {
//            return slidingMoves(board, myPosition, piece, new int[][] {
//                {1,1}, {1,-1}, {-1,1}, {-1,-1}
//            });
//        }
//        if (piece.getPieceType() == PieceType.ROOK) {
//            return slidingMoves(board, myPosition, piece, new int[][] {
//                    {1,0}, {-1,0}, {0,1}, {0,-1}
//            });
//        }
//        if (piece.getPieceType() == PieceType.QUEEN) {
//            return slidingMoves(board, myPosition, piece, new int[][] {
//                    {1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}
//            });
//        }
//        if (piece.getPieceType() == PieceType.KNIGHT) {
//            return jumpMoves(board, myPosition, piece, new int[][] {
//                    {2,1}, {1,2}, {-1,2}, {-2,1}, {-2,-1}, {-1,-2}, {1,-2}, {2,-1}
//            });
//        }
//        if (piece.getPieceType() == PieceType.KING) {
//            return jumpMoves(board, myPosition, piece, new int[][] {
//                {1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}
//            });
//        }
//        if (piece.getPieceType() == PieceType.PAWN) {
//            return pawnMoves(board, myPosition, piece);
//        }
//        return List.of();
    }


    private Collection<ChessMove> jumpMoves(
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

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition start, ChessPiece piece) {
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

    private void addPawnMove(List<ChessMove> moves, ChessPosition start, ChessPosition end, ChessPiece piece) {
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
