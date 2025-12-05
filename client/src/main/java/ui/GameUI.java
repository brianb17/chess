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
        return this.game; // Add a getter for the game
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
        if (perspective == Perspective.WHITE) {
            BoardUI.printBoard(game, BoardUI.Perspective.WHITE, highlightedMoves);
        } else {
            BoardUI.printBoard(game, BoardUI.Perspective.BLACK, highlightedMoves);
        }
        System.out.println();
        System.out.print("Enter a Command:");
    }

    public void drawInitialBoard() {
        drawBoard();
    }

    public void updateBoard(ChessGame updatedGame) {
        if (updatedGame == null) {
            System.out.println("ERROR: Received null game state from server.");
            return;
        }

        this.game = updatedGame;

        drawBoard();
    }


}
