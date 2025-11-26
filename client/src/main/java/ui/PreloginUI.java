package ui;

import client.ServerFacade;
import datamodel.AuthData;

import java.util.Scanner;

public class PreloginUI {

    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        System.out.println("Welcome to ♕ 240 Chess Client!");
        boolean running = true;

        while (running) {
            System.out.print("\nPrelogin> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "quit":
                    System.out.println("Goodbye!");
                    running = false;
                    System.exit(0);
                    break;

                case "register":
                    handleRegister();
                    break;

                case "login":
                    handleLogin();
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help     - Show this help message");
        System.out.println("  quit     - Exit the program");
        System.out.println("  register - Register a new user");
        System.out.println("  login    - Login with an existing account");
    }

    private void handleRegister() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            AuthData auth = facade.register(username, email, password);
            System.out.println("Registration successful! Logged in as " + auth.username());

            PostloginUI postlogin = new PostloginUI(facade, auth);
            postlogin.run();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleLogin() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            AuthData auth = facade.login(username, password);
            System.out.println("Login successful! Welcome " + auth.username());

            PostloginUI postlogin = new PostloginUI(facade, auth);
            postlogin.run();

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
}
