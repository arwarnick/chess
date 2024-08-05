package request;

public record CreateGameRequest(String gameName) {
    public CreateGameRequest {
        if (gameName == null || gameName.isEmpty()) {
            throw new IllegalArgumentException("Game name cannot be null or empty");
        }
    }
}