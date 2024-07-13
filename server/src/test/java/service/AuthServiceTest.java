package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import result.LoginResult;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService authService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        authService = new AuthService(userDAO, authDAO);

        // Add a test user
        userDAO.createUser(new UserData("testuser", "password", "test@example.com"));
    }

    @Test
    public void testSuccessfulLogin() throws DataAccessException {
        LoginRequest request = new LoginRequest("testuser", "password");
        LoginResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void testLoginWithWrongPassword() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> authService.login(request));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void testLoginNonexistentUser() {
        LoginRequest request = new LoginRequest("nonexistentuser", "password");
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> authService.login(request));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void testSuccessfulLogout() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        LoginResult loginResult = authService.login(loginRequest);

        assertDoesNotThrow(() -> authService.logout(loginResult.authToken()));
    }

    @Test
    public void testLogoutWithInvalidAuthToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> authService.logout("invalidauthtoken"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void testClear() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        LoginResult loginResult = authService.login(loginRequest);

        authService.clear();

        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> authService.logout(loginResult.authToken()));
        assertEquals("Error: unauthorized", exception.getMessage());
    }
}
