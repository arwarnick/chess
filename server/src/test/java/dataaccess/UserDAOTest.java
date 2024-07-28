package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        userDAO.clear();
    }

    @Test
    void createUserSuccess() throws DataAccessException {
        // Positive test
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals(user.username(), retrievedUser.username());
        assertEquals(user.password(), retrievedUser.password());
        assertEquals(user.email(), retrievedUser.email());
    }

    @Test
    void createUserDuplicate() throws DataAccessException {
        // Negative test
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.createUser(user);

        UserData duplicateUser = new UserData("testUser", "anotherPassword", "another@example.com");
        assertThrows(DataAccessException.class, () -> userDAO.createUser(duplicateUser));
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        // Positive test
        // Create multiple users
        UserData user1 = new UserData("user1", "password1", "user1@example.com");
        UserData user2 = new UserData("user2", "password2", "user2@example.com");
        userDAO.createUser(user1);
        userDAO.createUser(user2);

        // Retrieve and verify the second user
        UserData retrievedUser = userDAO.getUser("user2");
        assertNotNull(retrievedUser);
        assertEquals(user2.username(), retrievedUser.username());
        assertEquals(user2.password(), retrievedUser.password());
        assertEquals(user2.email(), retrievedUser.email());

        // Verify that the first user is different
        assertNotEquals(user1.username(), retrievedUser.username());
        assertNotEquals(user1.email(), retrievedUser.email());
    }

    @Test
    void getUserNonExistent() throws DataAccessException {
        // Negative test
        UserData retrievedUser = userDAO.getUser("nonExistentUser");
        assertNull(retrievedUser);
    }

    @Test
    void clearSuccess() throws DataAccessException {
        // Positive test
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.createUser(user);

        userDAO.clear();
        UserData retrievedUser = userDAO.getUser("testUser");
        assertNull(retrievedUser);
    }
}