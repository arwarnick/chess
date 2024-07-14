package dataaccess;

import model.AuthData;

/**
 * Interface for managing authentication data in the chess application.
 * This interface defines methods for creating, retrieving, and deleting authentication tokens.
 */
public interface AuthDAO {
    /**
     * Clears all stored authentication data.
     * This method is typically used for testing or resetting the system.
     */
    void clear();

    /**
     * Creates a new authentication token for a user.
     *
     * @param authToken The authentication token to be stored
     * @param username The username associated with the authentication token
     * @throws DataAccessException if there's an error while accessing the data store
     */
    void createAuth(String authToken, String username) throws DataAccessException;

    /**
     * Retrieves the authentication data associated with a given token.
     *
     * @param authToken The authentication token to look up
     * @return AuthData object if found, or null if not found
     * @throws DataAccessException if there's an error while accessing the data store
     */
    AuthData getAuth(String authToken) throws DataAccessException;

    /**
     * Deletes the authentication data associated with a given token.
     *
     * @param authToken The authentication token to be deleted
     * @throws DataAccessException if there's an error while accessing the data store
     */
    void deleteAuth(String authToken) throws DataAccessException;
}