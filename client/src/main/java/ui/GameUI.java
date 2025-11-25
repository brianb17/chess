package ui;

import chess.ChessGame;
import chess.ChessPiece;

public class GameUI {

    public enum Perspective { WHITE, BLACK }

    private final Perspective perspective;
    private final ChessPiece[][] board;

    public GameUI(Perspective perspective) {
        this.perspective = perspective;
        this.board = initializeBoard();
    }

    // Draw the board in the console
    public void drawInitialBoard() {
        if (perspective == Perspective.WHITE) {
            drawWhitePerspective();
        } else {
            drawBlackPerspective();
        }
    }

    // Initialize pieces for standard chess starting position
    private ChessPiece[][] initializeBoard() {
        ChessPiece[][] b = new ChessPiece[8][8];

        // Pawns
        for (int i = 0; i < 8; i++) {
            b[1][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            b[6][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }

        // Rooks
        b[0][0] = b[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        b[7][0] = b[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);

        // Knights
        b[0][1] = b[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        b[7][1] = b[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);

        // Bishops
        b[0][2] = b[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        b[7][2] = b[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);

        // Queens
        b[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        b[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);

        // Kings
        b[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        b[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);

        // Empty spaces
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                b[row][col] = null;
            }
        }

        return b;
    }

    private void drawWhitePerspective() {
        System.out.println("   a  b  c  d  e  f  g  h");
        for (int row = 7; row >= 0; row--) {
            System.out.print((row + 1) + " ");
            for (int col = 0; col < 8; col++) {
                printSquare(row, col);
            }
            System.out.println(" " + (row + 1));
        }
        System.out.println("   a  b  c  d  e  f  g  h");
    }

    private void drawBlackPerspective() {
        System.out.println("   h  g  f  e  d  c  b  a");
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " ");
            for (int col = 7; col >= 0; col--) {
                printSquare(row, col);
            }
            System.out.println(" " + (8 - row));
        }
        System.out.println("   h  g  f  e  d  c  b  a");
    }

    private void printSquare(int row, int col) {
        ChessPiece piece = board[row][col];
        String symbol = piece == null ? "\u2003" : piece.toString(); // em-space for empty
        System.out.print(symbol + " ");
    }
}
