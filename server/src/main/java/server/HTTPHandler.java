package server;

import com.google.gson.Gson;
import dataaccess.*;
import spark.*;
import service.*;
import request.*;
import result.*;

public class HTTPHandler {
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;
    private final Gson gson;

    public HTTPHandler() {
        UserDAO userDAO = new MySqlUserDAO();
        AuthDAO authDAO = new MySqlAuthDAO();
        GameDAO gameDAO = new MySqlGameDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.authService = new AuthService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);
        this.gson = new Gson();
    }

    public void registerEndpoints() {
        Spark.delete("/db", this::handleClearDatabase);
        Spark.post("/user", this::handleRegisterUser);
        Spark.post("/session", this::handleLoginUser);
        Spark.delete("/session", this::handleLogoutUser);
        Spark.get("/game", this::handleListGames);
        Spark.post("/game", this::handleCreateGame);
        Spark.put("/game", this::handleJoinGame);
    }

    private Object handleClearDatabase(Request request, Response response) {
        try {
            userService.clear();
            authService.clear();
            gameService.clear();
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            return handleException(e, response);
        }
    }

    private Object handleRegisterUser(Request request, Response response) {
        try {
            var registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
            RegisterResult result = userService.register(registerRequest);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return handleException(e, response);
        }
    }

    private Object handleLoginUser(Request request, Response response) {
        try {
            var loginRequest = gson.fromJson(request.body(), LoginRequest.class);
            LoginResult result = authService.login(loginRequest);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return handleException(e, response);
        }
    }

    private Object handleLogoutUser(Request request, Response response) {
        try {
            String authToken = request.headers("Authorization");
            authService.logout(authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            return handleException(e, response);
        }
    }

    private Object handleListGames(Request request, Response response) {
        try {
            String authToken = request.headers("Authorization");
            ListGamesResult result = gameService.listGames(authToken);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return handleException(e, response);
        }
    }

    private Object handleCreateGame(Request request, Response response) {
        try {
            String authToken = request.headers("Authorization");
            var createGameRequest = gson.fromJson(request.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(createGameRequest, authToken);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return handleException(e, response);
        }
    }

    private Object handleJoinGame(Request request, Response response) {
        try {
            String authToken = request.headers("Authorization");
            var joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            gameService.joinGame(joinGameRequest, authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            return handleException(e, response);
        } catch (IllegalArgumentException e) {
            response.status(400);
            return gson.toJson(new ErrorResult("Error: bad request"));
        }
    }

    private Object handleException(DataAccessException e, Response response) {
        ErrorResult errorResult = new ErrorResult(e.getMessage());
        response.status(determineHttpStatus(e));
        return gson.toJson(errorResult);
    }

    private int determineHttpStatus(DataAccessException e) {
        String message = e.getMessage().toLowerCase();
        if (message.contains("unauthorized")) {
            return 401;
        }
        if (message.contains("bad request")) {
            return 400;
        }
        if (message.contains("already taken")) {
            return 403;
        }
        return 500;
    }
}