package service;

import dataaccess.*;
import model.UserData;
import request.RegisterRequest;
import result.RegisterResult;
import org.mindrot.jbcrypt.BCrypt;

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

        // Hash the password
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        // Create new user
        UserData newUser = new UserData(request.username(), hashedPassword, request.email());
        userDAO.createUser(newUser);

        // Generate auth token
        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(authToken, request.username());

        return new RegisterResult(request.username(), authToken);
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
    }
}