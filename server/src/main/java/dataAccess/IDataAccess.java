package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;


public interface IDataAccess {
    // User-related operations
    void insertUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Game-related operations
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;

    // Authentication-related operations
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // Utility
    void clear() throws DataAccessException;
}

