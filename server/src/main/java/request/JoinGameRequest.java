package request;

public class JoinGameRequest {
    private final int gameID;
    private final String authToken;
    private final String playerColor; // Assuming the player can choose a color when joining.

    public JoinGameRequest(int gameId, String authToken, String playerColor) {
        this.gameID = gameId;
        this.authToken = authToken;
        this.playerColor = playerColor;
    }

    // Getters
    public int getGameID() {
        return gameID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getPlayerColor() {
        return playerColor;
    }
}

