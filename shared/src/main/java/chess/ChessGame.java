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
        BLACK
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
            return null;
        }
        HashSet<ChessMove> viableMoves = (HashSet<ChessMove>) thisPiece.pieceMoves(board, startPosition);
        HashSet<ChessMove> workingMoves = new HashSet<>();
        for (ChessMove move : viableMoves) {
            ChessPiece target = board.getPiece(move.getEndPosition());

            board.addPiece(startPosition, null);
            board.addPiece(move.getEndPosition(), thisPiece);
            if (!isInCheck(thisPiece.getTeamColor())) {
                workingMoves.add(move);
            }

            board.addPiece(move.getEndPosition(), target);
            board.addPiece(startPosition, thisPiece);
        }

        return workingMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> yesMoves = validMoves(move.getStartPosition());
        if (yesMoves == null) {
            throw new InvalidMoveException("No moves");
        }
        boolean isTurn = getTeamTurn() == board.getPosTeam(move.getStartPosition());
        if (yesMoves.contains(move) &&  isTurn) {
            ChessPiece movingPiece = board.getPiece(move.getStartPosition());
            if (move.getPromotionPiece() != null) {
                movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
            }

            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), movingPiece);
            if (getTeamTurn() == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
        }
        else {
            throw new InvalidMoveException("Nope");

        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = board.getKingSpot(teamColor);

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, pos)) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
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
                    Collection<ChessMove> moves = validMoves(pos);

                    if (moves != null) {
                        for (ChessMove move : moves) {
                            ChessBoard copyBoard = board.deepCopy();

                            ChessPiece movedPiece = new ChessPiece(
                                    piece.getTeamColor(),
                                    move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType()
                            );

                            copyBoard.addPiece(move.getStartPosition(), null);
                            copyBoard.addPiece(move.getEndPosition(), movedPiece);

                            ChessGame copyGame = new ChessGame();
                            copyGame.setBoard(copyBoard);

                            if (!copyGame.isInCheck(teamColor)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
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
