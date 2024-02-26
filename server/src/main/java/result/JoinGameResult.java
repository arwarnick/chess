package result;

public class JoinGameResult {
    private final boolean success;
    private final String message; // This can be used to send additional information back to the client.

    public JoinGameResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

