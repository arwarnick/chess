package result;

import model.UserData;

public class LoginResult {
    private String authToken;
    private String username;

    // Constructor
    public LoginResult(String authToken, UserData username) {
        this.authToken = authToken;
        this.username = username;
    }

    // Getters
    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }
}