package dataAccess;

public class ClearService {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clearAllData() throws DataAccessException {
        userDAO.clearUsers();
        gameDAO.clearGames();
        authDAO.clearAuths();
    }
}
