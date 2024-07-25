package service;

import dataaccess.*;
import model.UserData;
import request.LoginRequest;
import result.LoginResult;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class AuthService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public AuthService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = userDAO.getUser(request.username());
        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
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

    public void clear() throws DataAccessException {
        authDAO.clear();
    }
}