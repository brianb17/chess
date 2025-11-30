package ui;

import chess.ChessGame;

public class GameUI {

    public enum Perspective { WHITE, BLACK }

    private final Perspective perspective;
    private ChessGame game;

    public GameUI(ChessGame game, Perspective perspective) {
        this.game = game;
        this.perspective = perspective;
    }

    public void drawBoard() {
        if (perspective == Perspective.WHITE) {
            BoardUI.printBoard(game, BoardUI.Perspective.WHITE);
        } else {
            BoardUI.printBoard(game, BoardUI.Perspective.BLACK);
        }
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
