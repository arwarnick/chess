package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of the AuthDAO interface.
 * This class stores authentication data in a HashMap for quick access and modification.
 */
public class MemoryAuthDAO implements AuthDAO {
    /** Map to store authentication tokens and their associated data */
    private final Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() {
        auths.clear();
    }

    @Override
    public void createAuth(String authToken, String username) throws DataAccessException {
        auths.put(authToken, new AuthData(authToken, username));
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        auths.remove(authToken);
    }
}