package dataaccess;

/**
 * Custom exception class for data access errors in the chess application.
 * This exception is thrown when there are issues accessing or manipulating data in the DAO classes.
 */
public class DataAccessException extends Exception {
    /**
     * Constructs a new DataAccessException with the specified error message.
     *
     * @param message The error message describing the cause of the exception
     */
    public DataAccessException(String message) {
        super(message);
    }
}