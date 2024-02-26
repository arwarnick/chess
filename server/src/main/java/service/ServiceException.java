package service;

public class ServiceException extends Exception {

    // Constructor with a custom message
    public ServiceException(String message) {
        super(message);
    }

    // Constructor with a custom message and the cause of the exception
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

