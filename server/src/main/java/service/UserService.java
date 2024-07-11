package service;

import dataaccess.*;
import model.UserData;
import request.RegisterRequest;
import result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        // Check if user already exists
        if (userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("User already exists");
        }

        // Create new user
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);

        // Generate auth token
        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(authToken, request.username());

        return new RegisterResult(request.username(), authToken);
    }

    // Other methods like login, etc. would go here
}