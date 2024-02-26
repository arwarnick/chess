package result;

import model.UserData;

public class RegisterResult {
    private String authToken;
    private String username;

    // Constructor
    public RegisterResult(String authToken, UserData username) {
        this.authToken = authToken;
        this.username = String.valueOf(username);
    }

    // Getters
    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }
}
