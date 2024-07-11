package result;

public record LoginResult(String username, String authToken) {
    public LoginResult {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Auth token cannot be null or empty");
        }
    }
}