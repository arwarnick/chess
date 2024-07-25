package dataaccess;

import model.GameData;
import java.util.List;

/**
 * Interface for managing game data in the chess application.
 * This interface defines methods for creating, retrieving, and updating chess games.
 */
public interface GameDAO {
    /**
     * Clears all stored game data.
     * This method is typically used for testing or resetting the system.
     */
    void clear() throws DataAccessException;

    /**
     * Creates a new game in the data store.
     *
     * @param game The game data to be stored
     * @throws DataAccessException if there's an error while accessing the data store
     */
    void createGame(GameData game) throws DataAccessException;

    /**
     * Retrieves a game by its ID.
     *
     * @param gameID The ID of the game to retrieve
     * @return GameData object if found, or null if not found
     * @throws DataAccessException if there's an error while accessing the data store
     */
    GameData getGame(int gameID) throws DataAccessException;

    /**
     * Retrieves a list of all games in the data store.
     *
     * @return List of GameData objects
     * @throws DataAccessException if there's an error while accessing the data store
     */
    List<GameData> listGames() throws DataAccessException;

    /**
     * Updates an existing game in the data store.
     *
     * @param game The updated game data
     * @throws DataAccessException if there's an error while accessing the data store
     */
    void updateGame(GameData game) throws DataAccessException;
}