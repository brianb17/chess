package ui;

import chess.ChessGame;
import chess.ChessPosition;

import java.util.Collection;

public class GameUI {

    public enum Perspective { WHITE, BLACK }

    private final Perspective perspective;
    private ChessGame game;
    private Collection<ChessPosition> highlightedMoves;

    public GameUI(ChessGame game, Perspective perspective) {
        this.game = game;
        this.perspective = perspective;
    }

    public ChessGame getGame() {
        return this.game;
    }

    public Collection<ChessPosition> getHighlightedMoves() {
        return this.highlightedMoves;
    }

    public void setHighlightedMoves(Collection<ChessPosition> positions) {
        this.highlightedMoves = positions;
    }

    public void clearHighlights() {
        this.highlightedMoves = null;
    }

    public void drawBoard() {
        printBoardOnly();
        System.out.println();
        System.out.print("Enter a command: ");
    }


    private void printBoardOnly() {
        if (perspective == Perspective.WHITE) {
            BoardUI.printBoard(game, BoardUI.Perspective.WHITE, highlightedMoves);
        } else {
            BoardUI.printBoard(game, BoardUI.Perspective.BLACK, highlightedMoves);
        }
    }

    public void printNotification(String message) {
        System.out.println();
        System.out.println("Notification: " + message);
        System.out.println();
        System.out.print("Enter a command: ");
    }

    public void updateBoard(ChessGame updatedGame) {
        if (updatedGame == null) {
            System.out.println("ERROR: Received null game state from server.");
            return;
        }
        this.game = updatedGame;
        printBoardOnly();
        System.out.println();
        System.out.print("Enter a command: ");
    }


}
