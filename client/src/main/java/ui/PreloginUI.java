package ui;

import client.ServerFacade;
import result.LoginResult;
import result.RegisterResult;

import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade server;
    private final Scanner scanner;

    public PreloginUI(ServerFacade server) {
        this.server = server;
        this.scanner = new Scanner(System.in);
    }

    public LoginResult run() {
        System.out.println(EscapeSequences.WHITE_KING + " Welcome to the Chess Game! " + EscapeSequences.WHITE_KING);
        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + "Enter a command (Type 'help' for options): "
                    + EscapeSequences.RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "help":
                    displayHelp();
                    break;
                case "quit":
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Thanks for playing! Goodbye."
                            + EscapeSequences.RESET_TEXT_COLOR);
                    return null;
                case "login":
                    LoginResult loginResult = login();
                    if (loginResult != null) {
                        return loginResult;
                    }
                    break;
                case "register":
                    LoginResult registerResult = register();
                    if (registerResult != null) {
                        return registerResult;
                    }
                    break;
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command. Type 'help' for options."
                            + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void displayHelp() {

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Available commands:"
                + EscapeSequences.RESET_TEXT_COLOR);

        System.out.println("  help     - Display available commands");

        System.out.println("  quit     - Exit the program");

        System.out.println("  login    - Log in to an existing account");

        System.out.println("  register - Create a new account");

    }

    private LoginResult login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            LoginResult result = server.login(username, password);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Login successful. Welcome, "
                    + result.username() + "!" + EscapeSequences.RESET_TEXT_COLOR);
            return result;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Login failed: " + e.getMessage()
                    + EscapeSequences.RESET_TEXT_COLOR);
            return null;
        }
    }

    private LoginResult register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        try {
            RegisterResult result = server.register(username, password, email);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registration successful. Welcome, "
                    + result.username() + "!" + EscapeSequences.RESET_TEXT_COLOR);
            return new LoginResult(result.username(), result.authToken());
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Registration failed: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            return null;
        }
    }
}