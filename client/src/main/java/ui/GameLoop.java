package ui;

import chess.ChessMove;
import chess.ChessPosition;
import client.Websocket;

import java.util.Collection;
import java.util.Scanner;

public class GameLoop {

    private final Websocket ws;
    private final GameUI gameUI;
    private final String authToken;
    private final int gameID;
    private final String playerColor;

    public GameLoop(Websocket ws, GameUI gameUI, String authToken, int gameID, String playerColor) {
        this.ws = ws;
        this.gameUI = gameUI;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
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
                case "resign", "move", "highlight" -> {
                    if (playerColor == null) {
                        System.out.println("Observers can't use this command.");
                    }

                    switch (input) {
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
                        case "highlight" -> {
                            MoveReader moveReader = new MoveReader(scanner);
                            ChessPosition startPosition = moveReader.readPosition("Enter piece position (e.g., e2):");

                            if (startPosition != null) {
                                gameUI.clearHighlights();
                                Collection<ChessMove> moves = gameUI.getGame().validMoves(startPosition);

                                if (moves == null || moves.isEmpty()) {
                                    System.out.println("No legal moves for the piece at " + startPosition.toString());
                                    break;
                                }

                                Collection<ChessPosition> positionsToHighlight = new java.util.HashSet<>();
                                positionsToHighlight.add(startPosition);

                                for (ChessMove move : moves) {
                                    positionsToHighlight.add(move.getEndPosition());
                                }

                                gameUI.setHighlightedMoves(positionsToHighlight);
                                gameUI.drawBoard();
                            }
                        }
                    }
                }
                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }

        System.out.println("Exiting game loop...");
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help       - Show this help text");
        System.out.println("  redraw     - Redraw the chess board");
        System.out.println("  leave      - Leave the game");

        if (playerColor != null) {
            System.out.println("  move       - Make a move");
            System.out.println("  resign     - Resign the game");
            System.out.println("  highlight  - Highlight legal moves for a piece");
        }
    }
}
