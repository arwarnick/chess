package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.RegisterRequest;
import result.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void testSuccessfulRegistration() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("newuser", "password", "user@example.com");
        RegisterResult result = userService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.username());
        assertNotNull(result.authToken());

        UserData userData = userDAO.getUser("newuser");
        assertNotNull(userData);
        assertEquals("newuser", userData.username());
        assertEquals("password", userData.password());
        assertEquals("user@example.com", userData.email());
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
