package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of the UserDAO interface.
 * This class stores user data in a HashMap for quick access and modification.
 */
public class MemoryUserDAO implements UserDAO {
    /** Map to store usernames and their associated user data */
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Username already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }
}