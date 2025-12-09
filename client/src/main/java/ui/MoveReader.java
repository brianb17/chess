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
            // No need to check length here, RegEx already ensured length is 4 or 5
            ChessPosition start = parsePosition(input.substring(0, 2));
            ChessPosition end = parsePosition(input.substring(2, 4));
            ChessPiece.PieceType promotion = null;

            if (input.length() == 5) {
                // Since RegEx ensures the character is q, r, b, or n, the default case is simplified
                promotion = switch (input.charAt(4)) {
                    case 'q' -> ChessPiece.PieceType.QUEEN;
                    case 'r' -> ChessPiece.PieceType.ROOK;
                    case 'b' -> ChessPiece.PieceType.BISHOP;
                    case 'n' -> ChessPiece.PieceType.KNIGHT;
                    default -> null; // Should not happen due to RegEx
                };
            }

            return new ChessMove(start, end, promotion);

        } catch (IllegalArgumentException e) {
            // This catch block handles errors from parsePosition if the RegEx failed to catch something (unlikely)
            System.out.println("Invalid move coordinates: " + e.getMessage());
            return null;
        }
    }

    private ChessPosition parsePosition(String pos) {
        // Since we call this only with substrings of length 2, we can assume length is correct.
        // The characters are also guaranteed to be valid by the RegEx for 'a'-'h' and '1'-'8'.

        char file = pos.charAt(0); // a-h
        int rank = Character.getNumericValue(pos.charAt(1)); // 1-8

        // We can safely return the position as the RegEx ensures valid input.
        // Row (rank) is 1-8, Column (file) is 1-8.
        return new ChessPosition(rank, file - 'a' + 1);
    }

    public ChessPosition readPosition(String prompt) {
        System.out.print(prompt + " ");
        String input = scanner.nextLine().trim().toLowerCase();

        // 🛑 FIX 2: Use an explicit RegEx check for the single position reader as well
        if (!input.matches("^[a-h][1-8]$")) {
            System.out.println("Invalid position format. Must be two characters (e.g., 'e2').");
            return null;
        }

        try {
            return parsePosition(input);
        } catch (IllegalArgumentException e) {
            // Should be covered by the RegEx check
            System.out.println("Invalid coordinate: " + e.getMessage());
            return null;
        }
    }
}