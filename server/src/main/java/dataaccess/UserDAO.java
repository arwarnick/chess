package dataaccess;

import model.UserData;

/**
 * Interface for managing user data in the chess application.
 * This interface defines methods for creating and retrieving user information.
 */
public interface UserDAO {
    /**
     * Clears all stored user data.
     * This method is typically used for testing or resetting the system.
     */
    void clear();

    /**
     * Creates a new user in the data store.
     *
     * @param user The user data to be stored
     * @throws DataAccessException if there's an error while accessing the data store
     */
    void createUser(UserData user) throws DataAccessException;

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve
     * @return UserData object if found, or null if not found
     * @throws DataAccessException if there's an error while accessing the data store
     */
    UserData getUser(String username) throws DataAccessException;
}