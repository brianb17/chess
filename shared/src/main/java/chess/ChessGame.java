package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;

        public String toString() {
            return this == WHITE ? "white" : "black";
        }
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece thisPiece = board.getPiece(startPosition);


        if (thisPiece == null) {
            return new HashSet<>();
        }
        Collection<ChessMove> viableMoves = thisPiece.pieceMoves(board, startPosition);
        HashSet<ChessMove> validMoves = new HashSet<>();
        for (ChessMove move : viableMoves) {
            ChessPiece target = board.getPiece(move.getEndPosition());

            board.addPiece(startPosition, null);
            board.addPiece(move.getEndPosition(), thisPiece);
            if (!isInCheck(thisPiece.getTeamColor())) {
                validMoves.add(move);
            }

            board.addPiece(move.getEndPosition(), target);
            board.addPiece(startPosition, thisPiece);
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());

        if (movingPiece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        boolean isTurn = getTeamTurn() == movingPiece.getTeamColor();
        Collection<ChessMove> yesMoves = validMoves(move.getStartPosition());

        if (yesMoves.contains(move) && isTurn) {
            if (move.getPromotionPiece() != null) {
                movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
            }

            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), movingPiece);

            // Switch turns
            setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        } else {
            throw new InvalidMoveException("Invalid move for this turn");
        }
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (canAttackKing(new ChessPosition(row, col), teamColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAttackKing(ChessPosition pos, TeamColor kingColor) {
        ChessPiece piece = board.getPiece(pos);
        if (piece == null || piece.getTeamColor() == kingColor) {
            return false;
        }

        for (ChessMove move : piece.pieceMoves(board, pos)) {
            if (move.getEndPosition().equals(board.getKingSpot(kingColor))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (hasEscapingMove(piece, pos, teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean hasEscapingMove(ChessPiece piece, ChessPosition pos, TeamColor teamColor) {
        Collection<ChessMove> moves = validMoves(pos);
        if (moves == null) {
            return false;
        }

        for (ChessMove move : moves) {
            ChessBoard copyBoard = board.deepCopy();
            ChessPiece movedPiece = new ChessPiece(piece.getTeamColor(),
                    move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType());

            copyBoard.addPiece(move.getStartPosition(), null);
            copyBoard.addPiece(move.getEndPosition(), movedPiece);

            ChessGame copyGame = new ChessGame();
            copyGame.setBoard(copyBoard);

            if (!copyGame.isInCheck(teamColor)) {
                return true;
            }
        }
        return false;
    }



    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (hasValidMovesForPosition(new ChessPosition(row, col), teamColor)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasValidMovesForPosition(ChessPosition pos, TeamColor teamColor) {
        ChessPiece piece = board.getPiece(pos);
        if (piece == null || piece.getTeamColor() != teamColor) {
            return false;
        }

        Collection<ChessMove> moves = validMoves(pos);
        return moves != null && !moves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
