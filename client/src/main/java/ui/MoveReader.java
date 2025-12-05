package ui;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Scanner;

public class MoveReader {

    private final Scanner scanner;

    public MoveReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public ChessMove readMove() {
        System.out.print("Enter your move (e.g., e2e4 or e7e8q for promotion): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.length() < 4 || input.length() > 5) {
            System.out.println("Invalid move format.");
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
                    default -> {
                        System.out.println("Invalid promotion piece. Must be one of q, r, b, n.");
                        yield null;
                    }
                };
            }

            return new ChessMove(start, end, promotion);

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid move coordinates: " + e.getMessage());
            return null;
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) throw new IllegalArgumentException("Position must be 2 characters.");

        char file = pos.charAt(0); // a-h
        int rank = Character.getNumericValue(pos.charAt(1)); // 1-8

        return new ChessPosition(rank, file - 'a' + 1);
    }

    public ChessPosition readPosition(String prompt) {
        System.out.print(prompt + " ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.length() != 2) {
            System.out.println("Invalid position format. Must be two characters (e.g., 'e2').");
            return null;
        }

        char file = input.charAt(0);
        char rank = input.charAt(1);
        if (file < 'a' || file > 'h') {
            System.out.println("Invalid position: File must be between a and h.");
            return null;
        }
        if (rank < '1' || rank > '8') {
            System.out.println("Invalid position: Rank must be between 1 and 8.");
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
