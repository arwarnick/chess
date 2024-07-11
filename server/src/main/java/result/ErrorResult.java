package result;

public record ErrorResult(String message) {
    public ErrorResult {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
    }
}