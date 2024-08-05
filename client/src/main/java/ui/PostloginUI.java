package ui;

import chess.ChessGame;
import client.ServerFacade;
import model.GameData;
import result.CreateGameResult;
import result.ListGamesResult;

import java.util.Scanner;

public class PostloginUI {
    private final ServerFacade server;
    private final Scanner scanner;
    private final String authToken;

    public PostloginUI(ServerFacade server, String authToken) {
        this.server = server;
        this.scanner = new Scanner(System.in);
        this.authToken = authToken;
    }

    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Welcome to the Chess Game!"
                + EscapeSequences.RESET_TEXT_COLOR);
        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter a command (Type 'help' for options): "
                    + EscapeSequences.RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "help":
                    displayHelp();
                    break;
                case "logout":
                    if (logout()) {
                        return false;
                    }
                    break;
                case "quit":
                    return true;
                case "create":
                    createGame();
                    break;
                case "list":
                    listGames();
                    break;
                case "join":
                    joinGame();
                    break;
                case "observe":
                    observeGame();
                    break;
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command. Type 'help' for options."
                            + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:"
                + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("  help    - Display available commands");
        System.out.println("  logout  - Log out and return to the main menu");
        System.out.println("  create  - Create a new game");
        System.out.println("  list    - List all games");
        System.out.println("  join    - Join a game");
        System.out.println("  observe - Observe a game");
    }

    private boolean logout() {
        try {
            server.logout(authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged out successfully."
                    + EscapeSequences.RESET_TEXT_COLOR);
            return true;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Logout failed: " + e.getMessage()
                    + EscapeSequences.RESET_TEXT_COLOR);
            return false;
        }
    }

    private void createGame() {
        System.out.print("Enter game name: ");
        String gameName = scanner.nextLine();
        try {
            CreateGameResult result = server.createGame(gameName, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Game created successfully."
                    + EscapeSequences.RESET_TEXT_COLOR);
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to create game: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void listGames() {
        try {
            ListGamesResult result = server.listGames(authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Current Games:"
                    + EscapeSequences.RESET_TEXT_COLOR);
            int index = 1;
            for (GameData game : result.games()) {
                System.out.printf("%d. %s (White: %s, Black: %s)%n",
                        index++,
                        game.gameName(),
                        game.whiteUsername() != null ? game.whiteUsername() : "EMPTY",
                        game.blackUsername() != null ? game.blackUsername() : "EMPTY");
            }
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to list games: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void joinGame() {
        int gameNumber = -1;
        while (gameNumber == -1) {
            System.out.print("Enter game number: ");
            String input = scanner.nextLine();
            try {
                gameNumber = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid input. Please enter a number."
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        }

        String color = "";
        while (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.print("Enter color (WHITE/BLACK): ");
            color = scanner.nextLine().toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid color. Please enter WHITE or BLACK."
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        }

        try {
            ListGamesResult games = server.listGames(authToken);
            if (gameNumber <= 0 || gameNumber > games.games().size()) {
                throw new IllegalArgumentException("Invalid game number");
            }
            int gameId = games.games().get(gameNumber - 1).gameID();
            server.joinGame(color, gameId, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined the game."
                    + EscapeSequences.RESET_TEXT_COLOR);

            // Display the initial game state
            GameData gameData = server.getGame(gameId, authToken);
            ChessGame game = gameData.game();
            ChessboardUI boardUI = new ChessboardUI();
            boardUI.displayBoard(game);

            // Do not enter GameplayUI, just return to PostloginUI
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to join game: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void observeGame() {
        int gameNumber = -1;
        while (gameNumber == -1) {
            System.out.print("Enter game number to observe: ");
            String input = scanner.nextLine();
            try {
                gameNumber = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid input. Please enter a number."
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        }

        try {
            ListGamesResult games = server.listGames(authToken);
            if (gameNumber <= 0 || gameNumber > games.games().size()) {
                throw new IllegalArgumentException("Invalid game number");
            }
            int gameId = games.games().get(gameNumber - 1).gameID();
            server.joinGame("observer", gameId, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined the game as an observer."
                    + EscapeSequences.RESET_TEXT_COLOR);

            // Display the game state
            GameData gameData = server.getGame(gameId, authToken);
            ChessGame game = gameData.game();
            ChessboardUI boardUI = new ChessboardUI();

            boardUI.displayBoard(game);

        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to observe game: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }
}