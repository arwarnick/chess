package request;

public class CreateGameRequest {
    private final String gameName;

    public CreateGameRequest(String gameName) {
        this.gameName = gameName;
    }

    // Getter for gameName
    public String getGameName() {
        return gameName;
    }
}

