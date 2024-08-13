package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

public class ServerMessage {
    ServerMessageType serverMessageType;
    private String message;
    private String errorMessage;
    private Object game;  // Changed to Object to allow flexibility

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    // Existing method, must not be changed
    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    // New methods to set and get the message
    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    // New methods to set and get the error message
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    // New methods to set and get the game
    public void setGame(Object game) {
        this.game = game;
    }

    public Object getGame() {
        return this.game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType() &&
                Objects.equals(message, that.message) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType(), message, errorMessage, game);
    }
}