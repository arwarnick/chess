package dataAccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            throw new DataAccessException("Auth token not found");
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (!authTokens.containsKey(authToken)) {
            throw new DataAccessException("Auth token not found to delete");
        }
        authTokens.remove(authToken);
    }

    @Override
    public void clearAuths() {
        authTokens.clear();
    }
}
