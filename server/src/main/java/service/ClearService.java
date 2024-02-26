package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;

public class ClearService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clearDatabase() throws DataAccessException {
        // Clear all users, games, and auth tokens
        try {
            userDAO.clearUsers();
            gameDAO.clearGames();
            authDAO.clearAuths();
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear the database: " + e.getMessage(), e);
        }
    }
}

