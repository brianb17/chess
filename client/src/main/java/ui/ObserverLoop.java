package ui;

import client.Websocket;
import java.util.Scanner;

public class ObserverLoop {
    private final Websocket ws;
    private final GameUI gameUI;
    private final String authToken;
    private final int gameID;

    public ObserverLoop(Websocket ws, GameUI gameUI, String authToken, int gameID) {
        this.ws = ws;
        this.gameUI = gameUI;
        this.authToken = authToken;
        this.gameID = gameID;
    }


    public void start() {
        Scanner scanner = new Scanner(System.in);
        final boolean[] running = {true};

        System.out.println("Entering observer mode. Type 'help' for commands.");

        while (running[0]) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "redraw" -> gameUI.drawBoard();
                case "leave" -> {
                    ws.sendLeave(authToken, gameID);
                    running[0] = false;
                }
                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                Commands (Observer Mode):
                help       - Show this help text
                redraw     - Redraw the chess board
                leave      - Stop observing and return to the lobby
                """);
    }
}