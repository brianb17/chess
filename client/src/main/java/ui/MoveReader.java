package ui;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Scanner;

public class MoveReader {

    private final Scanner scanner;
    private static final String MOVE_REGEX = "^[a-h][1-8][a-h][1-8][qrbn]?$";

    public MoveReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public ChessMove readMove() {
        System.out.print("Enter your move (e.g., e2e4 or e7e8q for promotion): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (!input.matches(MOVE_REGEX)) {
            System.out.println("Invalid move format. Please use standard algebraic notation (e.g., e2e4 or g7g8q).");
            return null;
        }

        try {
            ChessPosition start = parsePosition(input.substring(0, 2));
            ChessPosition end = parsePosition(input.substring(2, 4));
            ChessPiece.PieceType promotion = null;

            if (input.length() == 5) {
                promotion = switch (input.charAt(4)) {
                    case 'q' -> ChessPiece.PieceType.QUEEN;
                    case 'r' -> ChessPiece.PieceType.ROOK;
                    case 'b' -> ChessPiece.PieceType.BISHOP;
                    case 'n' -> ChessPiece.PieceType.KNIGHT;
                    default -> null;
                };
            }

            return new ChessMove(start, end, promotion);

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid move coordinates: " + e.getMessage());
            return null;
        }
    }

    private ChessPosition parsePosition(String pos) {
        char file = pos.charAt(0);
        int rank = Character.getNumericValue(pos.charAt(1));
        return new ChessPosition(rank, file - 'a' + 1);
    }

    public ChessPosition readPosition(String prompt) {
        System.out.print(prompt + " ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (!input.matches("^[a-h][1-8]$")) {
            System.out.println("Invalid position format. Must be two characters (e.g., 'e2').");
            return null;
        }

        try {
            return parsePosition(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid coordinate: " + e.getMessage());
            return null;
        }
    }
}