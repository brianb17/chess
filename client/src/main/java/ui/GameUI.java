package ui;

import chess.ChessGame;

public class GameUI {

    public enum Perspective { WHITE, BLACK }

    private final Perspective perspective;
    private final ChessGame game;

    public GameUI(ChessGame game, Perspective perspective) {
        this.game = game;
        this.perspective = perspective;
    }

    public void drawInitialBoard() {
        if (perspective == Perspective.WHITE) {
            BoardUI.printBoard(game, BoardUI.Perspective.WHITE);
        } else {
            BoardUI.printBoard(game, BoardUI.Perspective.BLACK);
        }
    }
}
