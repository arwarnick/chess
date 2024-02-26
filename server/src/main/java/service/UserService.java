package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws ServiceException {
        try {
            // Check if the username is already taken
            if (userDAO.getUser(request.getUsername()) != null) {
                throw new ServiceException("Username is already taken.");
            }

            // Create new user data
            UserData newUser = new UserData(request.getUsername(), request.getPassword(), request.getEmail());
            userDAO.insertUser(newUser);

            // Create an auth token for the new user
            String authToken = generateAuthToken();
            AuthData authData = new AuthData(authToken, request.getUsername());
            authDAO.createAuth(authData);

            // Return the registration response
            return new RegisterResult(authToken, newUser);
        } catch (dataAccess.DataAccessException e) {
            throw new ServiceException("Failed to register user: " + e.getMessage(), e);
        }
    }

    public LoginResult login(LoginRequest request) throws ServiceException {
        try {
            // Check if the user exists and password matches
            UserData user = userDAO.getUser(request.getUsername());
            if (user == null || !user.password().equals(request.getPassword())) {
                throw new ServiceException("Invalid username or password.");
            }

            // Create a new auth token for the session
            String authToken = generateAuthToken();
            authDAO.createAuth(new AuthData(authToken, request.getUsername()));

            // Return the login response
            return new LoginResult(authToken, user);
        } catch (dataAccess.DataAccessException e) {
            throw new ServiceException("Failed to login: " + e.getMessage(), e);
        }
    }

    public void logout(String authToken) throws ServiceException {
        try {
            // Invalidate the auth token
            authDAO.deleteAuth(authToken);
        } catch (dataAccess.DataAccessException e) {
            e.printStackTrace();
        }
    }

    private String generateAuthToken() {
        // Implementation for generating a secure auth token
        // This is a placeholder and should be replaced with a real token generation implementation
        return UUID.randomUUID().toString();
    }
}

