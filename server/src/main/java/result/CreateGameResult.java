package result;

public class CreateGameResult {
    private final int gameID;
    private final String message; // Optional, for additional feedback

    public CreateGameResult(int gameID) {
        this.gameID = gameID;
        this.message = ""; // Default empty message
    }

    public CreateGameResult(int gameID, String message) {
        this.gameID = gameID;
        this.message = message;
    }

    // Getter for gameID
    public int getGameID() {
        return gameID;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }
}
