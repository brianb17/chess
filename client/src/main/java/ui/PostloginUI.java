package ui;

import chess.ChessGame;
import client.ServerFacade;
import client.Websocket;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.ListGamesResult;

import java.util.List;
import java.util.Scanner;

public class PostloginUI {

    private final ServerFacade facade;
    private AuthData auth;
    private final Scanner scanner = new Scanner(System.in);

    private List<GameData> lastListedGames;

    public PostloginUI(ServerFacade facade, AuthData auth) {
        this.facade = facade;
        this.auth = auth;
    }

    public void run() {
        System.out.println("\nWelcome to the Postlogin Menu! Type 'help' for options.");
        boolean running = true;

        while (running) {
            System.out.print("\nPostlogin> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "logout":
                    handleLogout();
                    running = false;
                    break;

                case "create game":
                    handleCreateGame();
                    break;

                case "list games":
                    handleListGames();
                    break;

                case "play game":
                    handlePlayGame();
                    break;

                case "observe game":
                    handleObserveGame();
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help         - Show this help message");
        System.out.println("  logout       - Logout and return to prelogin");
        System.out.println("  create game  - Create a new game");
        System.out.println("  list games   - List all existing games");
        System.out.println("  play game    - Join a game as a player");
        System.out.println("  observe game - Join a game as an observer");
    }

    private void handleLogout() {
        try {
            facade.logout(auth.authToken());
            System.out.println("Logged out successfully.");
            auth = null;
        } catch (Exception e) {
            System.out.println("Error logging out: " + e.getMessage());
        }
    }

    private void handleCreateGame() {
        try {
            System.out.print("Enter new game name: ");
            String gameName = scanner.nextLine().trim();
            int gameId = facade.createGame(auth.authToken(), gameName);
            System.out.println("Game '" + gameName + "' created with ID: " + gameId);
        } catch (Exception e) {
            System.out.println("Error creating game: " + e.getMessage());
        }
    }

    private void handleListGames() {
        try {
            ListGamesResult result = facade.listGames(auth.authToken());
            lastListedGames = result.games();

            if (lastListedGames.isEmpty()) {
                System.out.println("No games available.");
                return;
            }

            System.out.println("\nList of Games:");
            for (int i = 0; i < lastListedGames.size(); i++) {
                var game = lastListedGames.get(i);
                System.out.printf("%d. %s | White: %s | Black: %s%n",
                        i + 1,
                        game.gameName(),
                        game.whiteUsername() != null ? game.whiteUsername() : "-",
                        game.blackUsername() != null ? game.blackUsername() : "-");
            }
        } catch (Exception e) {
            System.out.println("Error listing games: " + e.getMessage());
        }
    }

    private void handlePlayGame() {
        if (lastListedGames == null || lastListedGames.isEmpty()) {
            System.out.println("Please list games first using 'list games'.");
            return;
        }

        try {
            System.out.print("Enter the number of the game you want to play: ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice < 1 || choice > lastListedGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            var gameData = lastListedGames.get(choice - 1);

            System.out.print("Enter color to play (WHITE or BLACK): ");
            String color = scanner.nextLine().trim().toUpperCase();
            facade.joinGame(auth.authToken(), gameData.gameID(), color);
            System.out.println("Joined game '" + gameData.gameName() + "' as " + color + ".");

            GameUI.Perspective perspective;
            if (color.equals("WHITE")) {
                perspective = GameUI.Perspective.WHITE;
            }
            else {
                perspective = GameUI.Perspective.BLACK;
            }
            GameUI gameUI = new GameUI(null, perspective);

            Websocket ws = new Websocket(gameUI);
            ws.connect(auth.authToken(), gameData.gameID());
            System.out.println("Connected to game server.");
            System.out.println("Waiting for server to send update to load game...");

            GameLoop loop = new GameLoop(ws, gameUI, auth.authToken(), gameData.gameID());
            loop.start();

//            ChessGame localGame = new ChessGame();
//            GameUI gameUI = new GameUI(localGame,
//                    color.equals("WHITE") ? GameUI.Perspective.WHITE : GameUI.Perspective.BLACK);
//            gameUI.drawInitialBoard();

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error joining game: " + e.getMessage());
        }
    }

    private void handleObserveGame() {
        if (lastListedGames == null || lastListedGames.isEmpty()) {
            System.out.println("Please list games first using 'list games'.");
            return;
        }

        try {
            System.out.print("Enter the number of the game to observe: ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice < 1 || choice > lastListedGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            var gameData = lastListedGames.get(choice - 1);
            System.out.println("Observing game '" + gameData.gameName() + "'.");

            ChessGame localGame = new ChessGame();
            GameUI gameUI = new GameUI(localGame, GameUI.Perspective.WHITE); // observers always see from white's perspective
            gameUI.drawInitialBoard();

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error observing game: " + e.getMessage());
        }
    }
}
