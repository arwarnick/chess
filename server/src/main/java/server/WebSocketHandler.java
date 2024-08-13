package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import model.GameData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final GameService gameService;
    private final Gson gson;
    private final Map<Integer, Set<Session>> gameSessions;

    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson();
        this.gameSessions = new ConcurrentHashMap<>();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        // Connection is handled when a CONNECT command is received
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        // Remove the session from all games it was connected to
        gameSessions.values().forEach(sessions -> sessions.remove(session));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            handleCommand(session, command);
        } catch (Exception e) {
            sendErrorMessage(session, "Error processing message: " + e.getMessage());
        }
    }

    private void handleCommand(Session session, UserGameCommand command) {
        try {
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session, command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
                default -> sendErrorMessage(session, "Unknown command type");
            }
        } catch (Exception e) {
            sendErrorMessage(session, "Error: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws Exception {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        // Validate authToken first
        try {
            gameService.getUsernameFromAuthToken(authToken);
        } catch (Exception e) {
            sendErrorMessage(session, "Error: unauthorized");
            return;
        }

        // Proceed with game validation and connection
        GameData game = gameService.getGame(gameID);
        if (game == null) {
            sendErrorMessage(session, "Invalid game ID");
            return;
        }

        // Add the session to the game's session set
        gameSessions.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);

        // Send a LOAD_GAME message to the connected client
        sendLoadGame(session, game);

        // Send a NOTIFICATION to other clients in the game
        String username = gameService.getUsernameFromAuthToken(authToken);
        sendNotificationToOthers(gameID, session, username + " has joined the game.");
    }

    private void handleMakeMove(Session session, UserGameCommand command) throws Exception {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        ChessMove move = command.getMove();

        // Check if the game is over
        if (gameService.isGameOver(gameID)) {
            sendErrorMessage(session, "Error: The game is already over");
            return;
        }

        // Validate and make the move
        try {
            GameData updatedGame = gameService.makeMove(gameID, authToken, move);

            // Send LOAD_GAME message to all clients in the game
            ServerMessage loadGameMessage = createLoadGameMessage(updatedGame);
            sendToGame(gameID, loadGameMessage);

            // Get the username of the player who made the move
            String username = gameService.getUsernameFromAuthToken(authToken);

            // Send NOTIFICATION to other players
            sendNotificationToOthers(gameID, session, username + " made a move: " + move.toString());

            // Check for checkmate, stalemate, or check
            ChessGame chess = updatedGame.game();
            if (chess.isInCheckmate(chess.getTeamTurn())) {
                sendNotificationToAll(gameID, "Checkmate! " + username + " wins!");
            } else if (chess.isInStalemate(chess.getTeamTurn())) {
                sendNotificationToAll(gameID, "Stalemate! The game is a draw.");
            } else if (chess.isInCheck(chess.getTeamTurn())) {
                sendNotificationToAll(gameID, "Check!");
            }
        } catch (Exception e) {
            sendErrorMessage(session, "Error: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) throws Exception {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        // Check if the game is already over
        if (gameService.isGameOver(gameID)) {
            sendErrorMessage(session, "Error: The game is already over");
            return;
        }

        try {
            // Get the username of the resigning player
            String username = gameService.getUsernameFromAuthToken(authToken);

            // Get the current game state
            GameData game = gameService.getGame(gameID);

            // Check if the user is actually a player in the game
            if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
                sendErrorMessage(session, "Error: Only players can resign from a game");
                return;
            }

            // Update the game state
            gameService.resignGame(gameID, authToken);

            // Send NOTIFICATION message to all clients in the game
            sendNotificationToAll(gameID, username + " has resigned from the game.");

        } catch (Exception e) {
            sendErrorMessage(session, "Error: " + e.getMessage());
        }
    }

    // New method to send a message to all sessions in a game except one
    private void sendToGameExcept(int gameID, ServerMessage message, Session exceptSession) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                if (session != exceptSession) {
                    sendMessage(session, message);
                }
            }
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws Exception {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        // Remove the session from the game's session set
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
        }

        // Update the game state (this won't throw an exception for observers now)
        gameService.leaveGame(gameID, authToken);

        // Send NOTIFICATION messages to other clients in the game
        String username = gameService.getUsernameFromAuthToken(authToken);
        sendNotificationToOthers(gameID, session, username + " has left the game.");
    }

    private void sendLoadGame(Session session, GameData game) {
        ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadGameMessage.setGame(game);
        sendMessage(session, loadGameMessage);
    }

    private ServerMessage createLoadGameMessage(GameData game) {
        ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadGameMessage.setGame(game);
        return loadGameMessage;
    }

    private void sendNotificationToAll(int gameID, String message) {
        ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(message);
        sendToGame(gameID, notificationMessage);
    }

    private void sendNotificationToOthers(int gameID, Session excludeSession, String message) {
        ServerMessage notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notificationMessage.setMessage(message);
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                if (session != excludeSession) {
                    sendMessage(session, notificationMessage);
                }
            }
        }
    }

    private void sendToGame(int gameID, ServerMessage message) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                sendMessage(session, message);
            }
        }
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(Session session, String errorMessage) {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        error.setErrorMessage(errorMessage);
        sendMessage(session, error);
    }
}