package ui;

import chess.*;

import java.util.Collection;

public class BoardUI {

    public enum Perspective { WHITE, BLACK }

    public static void printBoard(ChessGame game, Perspective perspective, Collection<ChessPosition> highlightedPositions) {
        var board = game.getBoard();
        System.out.println();

        if (perspective == Perspective.WHITE) {
            drawWhitePerspective(board, highlightedPositions);
        } else {
            drawBlackPerspective(board, highlightedPositions);
        }
    }

    // ---------------- WHITE VIEW ----------------
    private static void drawWhitePerspective(ChessBoard board, Collection<ChessPosition> highlightedPositions) {
        printColumnLabelsWhite();

        for (int row = 8; row >= 1; row--) {
            printRowNumber(row);

            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col, highlightedPositions);
            }

            printRowNumber(row);
            System.out.println(EscapeSequences.RESET);
        }

        printColumnLabelsWhite();
    }

    // ---------------- BLACK VIEW ----------------
    private static void drawBlackPerspective(ChessBoard board, Collection<ChessPosition> highlightedPositions) {
        printColumnLabelsBlack();

        for (int row = 1; row <= 8; row++) {
            printRowNumber(row);

            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col, highlightedPositions);
            }

            printRowNumber(row);
            System.out.println(EscapeSequences.RESET);
        }

        printColumnLabelsBlack();
    }

    // ---------------- UTILS ----------------

    private static void printSquare(ChessBoard board, int row, int col, Collection<ChessPosition> highlightedPositions) {
        var piece = board.getPiece(new ChessPosition(row, col));
        ChessPosition currentPosition = new ChessPosition(row, col);

        boolean lightSquare = ((row + col) % 2 == 0);
        String bg = lightSquare ? EscapeSequences.LIGHT_BG : EscapeSequences.DARK_BG;

        if (highlightedPositions != null && highlightedPositions.contains(currentPosition)) {
            bg = lightSquare ? EscapeSequences.HIGHLIGHT_LIGHT_BG : EscapeSequences.HIGHLIGHT_DARK_BG;
        }

        System.out.print(bg);

        if (piece == null) {
            System.out.print(" \u2003 ");
        } else {
            System.out.print(" " + getPieceSymbol(piece) + " ");
        }

        System.out.print(EscapeSequences.RESET);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getTeamColor()) {
            case WHITE -> EscapeSequences.WHITE_PIECE + unicodePiece(piece);
            case BLACK -> EscapeSequences.BLACK_PIECE + unicodePiece(piece);
        };
    }

    private static String unicodePiece(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "♔";
            case QUEEN -> "♕";
            case ROOK -> "♖";
            case BISHOP -> "♗";
            case KNIGHT -> "♘";
            case PAWN -> "♙";
        };
    }


    private static void printColumnLabelsWhite() {
        System.out.print("   ");
        boolean bigPrint = true;
        int counter = 0;
        for (char c = 'a'; c <= 'h'; c++) {
            if (bigPrint) {
                System.out.print(" " + c + "  ");
            } else {
                System.out.print(" " + c + " ");
            }
            bigPrint = !bigPrint;
            counter++;
            if (counter == 4) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    private static void printColumnLabelsBlack() {
        System.out.print("   ");
        boolean bigPrint = true;
        int counter = 0;
        for (char c = 'h'; c >= 'a'; c--) {
            if (bigPrint) {
                System.out.print(" " + c + "  ");
            } else {
                System.out.print(" " + c + " ");
            }
            bigPrint = !bigPrint;
            counter++;
            if (counter == 4) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    private static void printRowNumber(int row) {
        System.out.print(" " + row + " ");
    }
}
