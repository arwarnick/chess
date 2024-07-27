package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.RegisterRequest;
import result.RegisterResult;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        userService = new UserService(userDAO, authDAO);

        // Clear the database before each test
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    public void testSuccessfulRegistration() throws DataAccessException {
        String username = "newuser";
        String password = "password";
        String email = "user@example.com";

        RegisterRequest request = new RegisterRequest(username, password, email);
        RegisterResult result = userService.register(request);

        assertNotNull(result);
        assertEquals(username, result.username());
        assertNotNull(result.authToken());

        UserData userData = userDAO.getUser(username);
        assertNotNull(userData);
        assertEquals(username, userData.username());
        assertTrue(BCrypt.checkpw(password, userData.password()), "Password should be correctly hashed");
        assertEquals(email, userData.email());
    }

    @Test
    public void testRegisterExistingUser() {
        RegisterRequest request = new RegisterRequest("existinguser", "password", "user@example.com");
        
        assertDoesNotThrow(() -> userService.register(request));
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> userService.register(request));
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void testRegisterInvalidRequest() {
        RegisterRequest invalidRequest = new RegisterRequest("", "", "");
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> userService.register(invalidRequest));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void testClear() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("user", "password", "user@example.com");
        userService.register(request);

        assertNotNull(userDAO.getUser("user"));

        userService.clear();

        assertNull(userDAO.getUser("user"));
    }
}
