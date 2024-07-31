package ui;

import client.ServerFacade;
import result.LoginResult;
import result.RegisterResult;
import ui.EscapeSequences;

import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade server;
    private final Scanner scanner;

    public PreloginUI(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println(EscapeSequences.WHITE_KING + " Welcome to the Chess Game! " + EscapeSequences.WHITE_KING);
        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + "Enter a command (Type 'help' for options): " + EscapeSequences.RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "help":
                    displayHelp();
                    break;
                case "quit":
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Thanks for playing! Goodbye." + EscapeSequences.RESET_TEXT_COLOR);
                    return;
                case "login":
                    if (login()) {
                        return; // Exit prelogin if login successful
                    }
                    break;
                case "register":
                    if (register()) {
                        return; // Exit prelogin if registration successful
                    }
                    break;
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command. Type 'help' for options." + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("  help     - Display available commands");
        System.out.println("  quit     - Exit the program");
        System.out.println("  login    - Log in to an existing account");
        System.out.println("  register - Create a new account");
    }

    private boolean login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            LoginResult result = server.login(username, password);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Login successful. Welcome, " + result.username() + "!" + EscapeSequences.RESET_TEXT_COLOR);
            return true;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Login failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            return false;
        }
    }

    private boolean register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        try {
            RegisterResult result = server.register(username, password, email);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registration successful. Welcome, " + result.username() + "!" + EscapeSequences.RESET_TEXT_COLOR);
            return true;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Registration failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            return false;
        }
    }
}