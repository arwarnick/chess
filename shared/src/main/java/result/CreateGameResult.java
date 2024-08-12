package result;

public record CreateGameResult(int gameID) {
    public CreateGameResult {
        if (gameID <= 0) {
            throw new IllegalArgumentException("Game ID must be a positive integer");
        }
    }

    public int getGameID() {
        return gameID;
    }
}