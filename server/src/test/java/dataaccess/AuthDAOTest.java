package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthDAOTest {
    private AuthDAO authDAO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        userDAO = new MySqlUserDAO();
        authDAO.clear();
        userDAO.clear();

        // Create a test user
        UserData testUser = new UserData("testUser", "password", "test@example.com");
        userDAO.createUser(testUser);
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        // Positive test
        String authToken = UUID.randomUUID().toString();
        String username = "testUser";
        authDAO.createAuth(authToken, username);

        AuthData retrievedAuth = authDAO.getAuth(authToken);
        assertNotNull(retrievedAuth);
        assertEquals(authToken, retrievedAuth.authToken());
        assertEquals(username, retrievedAuth.username());
    }

    @Test
    void createAuthNonExistentUser() {
        // Negative test
        String authToken = UUID.randomUUID().toString();
        String username = "nonExistentUser";
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(authToken, username));
    }

    @Test
    void getAuthSuccess() throws DataAccessException {
        // Positive test
        String authToken = UUID.randomUUID().toString();
        String username = "testUser";
        authDAO.createAuth(authToken, username);

        AuthData retrievedAuth = authDAO.getAuth(authToken);
        assertNotNull(retrievedAuth);
        assertEquals(authToken, retrievedAuth.authToken());
        assertEquals(username, retrievedAuth.username());
    }

    @Test
    void getAuthNonExistent() throws DataAccessException {
        // Negative test
        AuthData retrievedAuth = authDAO.getAuth(UUID.randomUUID().toString());
        assertNull(retrievedAuth);
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        // Positive test
        String authToken = UUID.randomUUID().toString();
        String username = "testUser";
        authDAO.createAuth(authToken, username);

        authDAO.deleteAuth(authToken);
        AuthData retrievedAuth = authDAO.getAuth(authToken);
        assertNull(retrievedAuth);
    }

    @Test
    void deleteAuthNonExistent() {
        // Negative test
        String nonExistentToken = UUID.randomUUID().toString();
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(nonExistentToken));
    }

    @Test
    void clearSuccess() throws DataAccessException {
        // Positive test
        String authToken = UUID.randomUUID().toString();
        String username = "testUser";
        authDAO.createAuth(authToken, username);

        authDAO.clear();
        AuthData retrievedAuth = authDAO.getAuth(authToken);
        assertNull(retrievedAuth);
    }
}