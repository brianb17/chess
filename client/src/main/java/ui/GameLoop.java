package ui;

import chess.ChessMove;
import client.Websocket;

import java.util.Scanner;

public class GameLoop {

    private final Websocket ws;
    private final GameUI gameUI;
    private final String authToken;
    private final int gameID;

    public GameLoop(Websocket ws, GameUI gameUI, String authToken, int gameID) {
        this.ws = ws;
        this.gameUI = gameUI;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        final boolean[] running = {true};

        Thread heartbeat = new Thread(() -> {
            try {
                while (running[0]) {
                    ws.sendHeartbeat(authToken, gameID);
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
            }
        });
        heartbeat.setDaemon(true);
        heartbeat.start();

        System.out.println("Entering game loop. Type 'help' for commands.");

        while (running[0]) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "redraw" -> gameUI.drawBoard();
                case "leave" -> {
                    ws.sendLeave(authToken, gameID);
                    running[0] = false;
                    heartbeat.interrupt();
                }
                case "resign" -> {
                    ws.sendResign(authToken, gameID);
                    running[0] = false;
                    heartbeat.interrupt();
                }
                case "move" -> {
                    MoveReader moveReader = new MoveReader(scanner);
                    ChessMove move = moveReader.readMove();
                    if (move != null) {
                        ws.sendMove(authToken, gameID, move);
                    }
                }
                case "highlight" -> System.out.println("Highlighting not implemented in console.");
                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }

        System.out.println("Exiting game loop...");
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                help       - Show this help text
                redraw     - Redraw the chess board
                move       - Make a move
                resign     - Resign the game
                leave      - Leave the game
                highlight  - Highlight legal moves for a piece (not implemented)
                """);
    }
}
