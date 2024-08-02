package ui;

import chess.*;
import client.ServerFacade;
import model.GameData;
import ui.EscapeSequences;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI {
    private final ServerFacade server;
    private final String authToken;
    private final int gameId;
    private ChessGame game;
    private final ChessboardUI boardUI;
    private final Scanner scanner;
    private final boolean isObserver;
    private final ChessGame.TeamColor playerColor;

    public GameplayUI(ServerFacade server, String authToken, int gameId, ChessGame.TeamColor playerColor) {
        this.server = server;
        this.authToken = authToken;
        this.gameId = gameId;
        this.boardUI = new ChessboardUI();
        this.scanner = new Scanner(System.in);
        this.isObserver = (playerColor == null);
        this.playerColor = playerColor;
        initializeGame();
    }

    private void initializeGame() {
        try {
            GameData gameData = server.getGame(gameId, authToken);
            this.game = gameData.game();
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to initialize game: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            this.game = new ChessGame(); // Fallback to a new game if fetching fails
        }
    }

    public void run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Welcome to Chess Game #" + gameId + "!" + EscapeSequences.RESET_TEXT_COLOR);
        if (isObserver) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "You are observing this game." + EscapeSequences.RESET_TEXT_COLOR);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "You are playing as " + playerColor + "." + EscapeSequences.RESET_TEXT_COLOR);
        }

        while (true) {
            displayGame();
            if (isObserver || game.getTeamTurn() != playerColor) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Waiting for other player's move..." + EscapeSequences.RESET_TEXT_COLOR);
                // In a real implementation, we would wait for server updates here
                break; // For now, we'll just exit the game loop
            }
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter a command (or 'help' for options): " + EscapeSequences.RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    displayHelp();
                    break;
                case "quit":
                    return;
                case "move":
                    makeMove();
                    break;
                case "resign":
                    if (resign()) return;
                    break;
                case "redraw":
                    // No action needed, the board will be redrawn in the next loop iteration
                    break;
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command. Type 'help' for options." + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void displayGame() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Current Game State:" + EscapeSequences.RESET_TEXT_COLOR);
        boardUI.displayBoard(game, playerColor == ChessGame.TeamColor.WHITE);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Current turn: " + game.getTeamTurn() + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("  help   - Display this help message");
        System.out.println("  quit   - Exit the game");
        System.out.println("  move   - Make a move");
        System.out.println("  resign - Resign from the game");
        System.out.println("  redraw - Redraw the game board");
    }

    private void makeMove() {
        if (isObserver) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Observers cannot make moves." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        if (game.getTeamTurn() != playerColor) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "It's not your turn." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }

        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Enter your move (e.g., e2 e4):" + EscapeSequences.RESET_TEXT_COLOR);
        String moveInput = scanner.nextLine().trim().toLowerCase();
        String[] parts = moveInput.split(" ");
        if (parts.length != 2) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid move format. Use 'start_position end_position' (e.g., e2 e4)" + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }

        try {
            ChessPosition startPosition = parsePosition(parts[0]);
            ChessPosition endPosition = parsePosition(parts[1]);

            Collection<ChessMove> validMoves = game.validMoves(startPosition);
            ChessMove move = null;
            for (ChessMove validMove : validMoves) {
                if (validMove.getEndPosition().equals(endPosition)) {
                    move = validMove;
                    break;
                }
            }

            if (move != null) {
                game.makeMove(move);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Move made successfully." + EscapeSequences.RESET_TEXT_COLOR);
                // Here you would typically send the move to the server
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid move. Please try again." + EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (InvalidMoveException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid move: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        } catch (IllegalArgumentException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid position. Please use algebraic notation (e.g., e2)." + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private ChessPosition parsePosition(String posStr) {
        if (posStr.length() != 2) {
            throw new IllegalArgumentException("Invalid position string");
        }
        int col = posStr.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(posStr.charAt(1));
        return new ChessPosition(row, col);
    }

    private boolean resign() {
        if (isObserver) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Observers cannot resign." + EscapeSequences.RESET_TEXT_COLOR);
            return false;
        }
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no)" + EscapeSequences.RESET_TEXT_COLOR);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes")) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "You have resigned from the game." + EscapeSequences.RESET_TEXT_COLOR);
            // Here you would typically notify the server about the resignation
            return true;
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Resignation cancelled." + EscapeSequences.RESET_TEXT_COLOR);
            return false;
        }
    }
}