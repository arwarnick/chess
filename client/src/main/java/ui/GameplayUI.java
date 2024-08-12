package ui;

import chess.*;
import client.ServerFacade;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements ServerFacade.ServerMessageObserver {
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
            server.connectToWebSocket("ws://localhost:8080/ws", this);

            // Send CONNECT command
            UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
            server.sendCommand(connectCommand);
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to initialize game: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            this.game = new ChessGame(); // Fallback to a new game if fetching fails
        }
    }

    public void run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Welcome to Chess Game #" + gameId + "!"
                + EscapeSequences.RESET_TEXT_COLOR);
        if (isObserver) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "You are observing this game."
                    + EscapeSequences.RESET_TEXT_COLOR);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "You are playing as " + playerColor
                    + "." + EscapeSequences.RESET_TEXT_COLOR);
        }

        while (true) {
            displayGame();
            if (isObserver || game.getTeamTurn() != playerColor) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Waiting for other player's move..."
                        + EscapeSequences.RESET_TEXT_COLOR);
                break; // For now, we'll just exit the game loop
            }
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter a command (or 'help' for options): "
                    + EscapeSequences.RESET_TEXT_COLOR);
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
                    if (resign()) {
                        return;
                    }
                    break;
                case "redraw":
                    // No action needed, the board will be redrawn in the next loop iteration
                    break;
                case "highlight":
                    highlightMoves();
                    break;
                default:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid command. Type 'help' for options."
                            + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void displayGame() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Current Game State:"
                + EscapeSequences.RESET_TEXT_COLOR);

        boardUI.displayBoard(game, playerColor);

        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Current turn: "
                + game.getTeamTurn() + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:"
                + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("  help   - Display this help message");
        System.out.println("  quit   - Exit the game");
        System.out.println("  move   - Make a move");
        System.out.println("  resign - Resign from the game");
        System.out.println("  redraw - Redraw the game board");
        System.out.println("  highlight - Highlight legal moves for a piece");
    }

    private void makeMove() {
        if (isObserver) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Observers cannot make moves."
                    + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        if (game.getTeamTurn() != playerColor) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "It's not your turn."
                    + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }

        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Enter your move (e.g., e2 e4):"
                + EscapeSequences.RESET_TEXT_COLOR);
        String moveInput = scanner.nextLine().trim().toLowerCase();
        String[] parts = moveInput.split(" ");
        if (parts.length != 2 && parts.length != 3) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                    + "Invalid move format. Use 'start_position end_position [promotion_piece]' (e.g., e2 e4 or e7 e8 q)"
                    + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }

        try {
            ChessPosition startPosition = parsePosition(parts[0]);
            ChessPosition endPosition = parsePosition(parts[1]);
            ChessPiece.PieceType promotionPiece = null;
            if (parts.length == 3) {
                promotionPiece = parsePromotionPiece(parts[2]);
            }

            ChessMove move = new ChessMove(startPosition, endPosition, promotionPiece);

            if (game.validMoves(startPosition).contains(move)) {
                UserGameCommand moveCommand = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId);
                moveCommand.setMove(moveInput);
                server.sendCommand(moveCommand);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Move sent to server."
                        + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid move. Please try again."
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error sending move: " + e.getMessage()
                    + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private boolean resign() {
        if (isObserver) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Observers cannot resign."
                    + EscapeSequences.RESET_TEXT_COLOR);
            return false;
        }
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no)"
                + EscapeSequences.RESET_TEXT_COLOR);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes")) {
            try {
                UserGameCommand resignCommand = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
                server.sendCommand(resignCommand);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "You have resigned from the game."
                        + EscapeSequences.RESET_TEXT_COLOR);
                return true;
            } catch (Exception e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error resigning: " + e.getMessage()
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Resignation cancelled."
                    + EscapeSequences.RESET_TEXT_COLOR);
        }
        return false;
    }

    private void highlightMoves() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Enter the position of the piece to highlight (e.g., e2):"
                + EscapeSequences.RESET_TEXT_COLOR);
        String positionInput = scanner.nextLine().trim().toLowerCase();
        try {
            ChessPosition position = parsePosition(positionInput);
            Collection<ChessMove> validMoves = game.validMoves(position);
            boardUI.displayBoardWithHighlights(game, playerColor, position, validMoves);
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid position. Please use algebraic notation (e.g., e2)."
                    + EscapeSequences.RESET_TEXT_COLOR);
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

    private ChessPiece.PieceType parsePromotionPiece(String pieceStr) {
        return switch (pieceStr.toLowerCase()) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "b" -> ChessPiece.PieceType.BISHOP;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece");
        };
    }

    @Override
    public void onServerMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                handleLoadGame(message);
                break;
            case ERROR:
                handleError(message);
                break;
            case NOTIFICATION:
                handleNotification(message);
                break;
        }
    }

    private void handleLoadGame(ServerMessage message) {
        GameData updatedGame = (GameData) message.getGame();
        if (updatedGame != null) {
            this.game = updatedGame.game();
            displayGame();
        }
    }

    private void handleError(ServerMessage message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message.getErrorMessage()
                + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleNotification(ServerMessage message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Notification: " + message.getMessage()
                + EscapeSequences.RESET_TEXT_COLOR);
    }
}