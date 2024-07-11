package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void clear();
    void createAuth(String authToken, String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
}