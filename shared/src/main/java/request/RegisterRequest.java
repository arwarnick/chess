package request;

public record RegisterRequest(String username, String password, String email) {

    // Methods to check if fields are valid
    public boolean isValid() {
        return isUsernameValid() && isPasswordValid() && isEmailValid();
    }

    public boolean isUsernameValid() {
        return username != null && !username.isEmpty();
    }

    public boolean isPasswordValid() {
        return password != null && !password.isEmpty();
    }

    public boolean isEmailValid() {
        return email != null && !username.isEmpty();
    }
}