package server;

import com.google.gson.Gson;
import spark.*;
import service.*;
import dataaccess.*;
import request.*;
import result.*;

/**
 * The main server class for the chess application.
 * This class sets up and manages the HTTP endpoints for the chess server.
 */
public class Server {
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;
    private final Gson gson;

    /**
     * Constructs a new Server instance.
     * Initializes services with in-memory data access objects.
     */
    public Server() {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.authService = new AuthService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);
        this.gson = new Gson();

        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
            // ... rest of initialization code
        } catch (DataAccessException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Starts the server on the specified port.
     *
     * @param desiredPort the port to run the server on
     * @return the actual port the server is running on
     */
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        setupEndpoints();
        setupExceptionHandling();

        Spark.awaitInitialization();
        return Spark.port();
    }

    /**
     * Sets up all the HTTP endpoints for the server.
     */
    private void setupEndpoints() {
        Spark.delete("/db", this::handleClearDatabase);
        Spark.post("/user", this::handleRegisterUser);
        Spark.post("/session", this::handleLoginUser);
        Spark.delete("/session", this::handleLogoutUser);
        Spark.get("/game", this::handleListGames);
        Spark.post("/game", this::handleCreateGame);
        Spark.put("/game", this::handleJoinGame);
    }

    /**
     * Sets up exception handling for the server.
     */
    private void setupExceptionHandling() {
        Spark.exception(DataAccessException.class, this::handleDataAccessException);
        Spark.exception(Exception.class, this::handleGenericException);
    }

    /**
     * Handles the clear database endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return an empty JSON object as a string
     */
    private Object handleClearDatabase(Request request, Response response) throws DataAccessException {
        userService.clear();
        authService.clear();
        gameService.clear();
        response.status(200);
        return "{}";
    }

    /**
     * Handles the register user endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return a JSON string representing the RegisterResult
     * @throws DataAccessException if registration fails
     */
    private Object handleRegisterUser(Request request, Response response) throws DataAccessException {
        var registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
        try {
            RegisterResult result = userService.register(registerRequest);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            throw e;
        }
    }

    /**
     * Handles the login user endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return a JSON string representing the LoginResult
     * @throws DataAccessException if login fails
     */
    private Object handleLoginUser(Request request, Response response) throws DataAccessException {
        var loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        try {
            LoginResult result = authService.login(loginRequest);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            throw e;
        }
    }

    /**
     * Handles the logout user endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return an empty JSON object as a string
     * @throws DataAccessException if logout fails
     */
    private Object handleLogoutUser(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        try {
            authService.logout(authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            throw e;
        }
    }

    /**
     * Handles the list games endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return a JSON string representing the ListGamesResult
     * @throws DataAccessException if listing games fails
     */
    private Object handleListGames(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        try {
            ListGamesResult result = gameService.listGames(authToken);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            throw e;
        }
    }

    /**
     * Handles the create game endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return a JSON string representing the CreateGameResult
     * @throws DataAccessException if game creation fails
     */
    private Object handleCreateGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        var createGameRequest = gson.fromJson(request.body(), CreateGameRequest.class);
        try {
            CreateGameResult result = gameService.createGame(createGameRequest, authToken);
            response.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            throw e;
        }
    }

    /**
     * Handles the join game endpoint.
     *
     * @param request the Spark request object
     * @param response the Spark response object
     * @return an empty JSON object as a string
     * @throws DataAccessException if joining the game fails
     */
    private Object handleJoinGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        var joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
        try {
            gameService.joinGame(joinGameRequest, authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Error: bad request");
        }
    }

    /**
     * Handles DataAccessExceptions by setting the appropriate HTTP status and error message.
     *
     * @param e the caught DataAccessException
     * @param request the Spark request object
     * @param response the Spark response object
     */
    private void handleDataAccessException(DataAccessException e, Request request, Response response) {
        ErrorResult errorResult = new ErrorResult(e.getMessage());
        response.status(determineHttpStatus(e));
        response.body(gson.toJson(errorResult));
    }

    /**
     * Handles generic exceptions by setting a 500 Internal Server Error status.
     *
     * @param e the caught Exception
     * @param request the Spark request object
     * @param response the Spark response object
     */
    private void handleGenericException(Exception e, Request request, Response response) {
        ErrorResult errorResult = new ErrorResult("Internal server error");
        response.status(500);
        response.body(gson.toJson(errorResult));
    }

    /**
     * Determines the appropriate HTTP status code based on the exception message.
     *
     * @param e the DataAccessException
     * @return the appropriate HTTP status code
     */
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

    /**
     * Stops the server.
     */
    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}