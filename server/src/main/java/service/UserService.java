package service;

import dataaccess.*;
import model.UserData;
import request.RegisterRequest;
import request.LoginRequest;
import result.RegisterResult;
import result.LoginResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        // Validate the request
        if (!request.isValid()) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if user already exists
        if (userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Create new user
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);

        // Generate auth token
        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(authToken, request.username());

        return new RegisterResult(request.username(), authToken);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(authToken, request.username());

        return new LoginResult(request.username(), authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        authDAO.deleteAuth(authToken);
    }

    public void clear() {
        userDAO.clear();
    }
}