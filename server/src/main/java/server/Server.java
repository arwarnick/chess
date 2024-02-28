package server;

import com.google.gson.Gson;
import dataAccess.MemoryAuthDAO;
import dataAccess.MemoryGameDAO;
import dataAccess.MemoryUserDAO;
import request.*;
import result.*;
import service.UserService;
import spark.Service;
import service.*;
import spark.Spark;

public class Server {

    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server(UserService userService, GameService gameService, ClearService clearService) {
        this.userService = userService;
        this.gameService = gameService;
        this.clearService = clearService;
    }

    public Server() {
        // Initialize default services with in-memory DAOs or mocks as placeholders
        // This part needs to be adjusted based on how you instantiate your DAOs and services
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO);
        this.clearService = new ClearService(userDAO, gameDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        // User Registration endpoint
        Spark.post("/user", (request, response) -> {
            Gson gson = new Gson();
            RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
            try {
                RegisterResult registerResult = userService.register(registerRequest);
                response.status(200); // OK
                response.type("application/json");
                return gson.toJson(registerResult);
            } catch (ServiceException e) {
                // Handle registration errors
                response.status(400); // Bad Request or other appropriate status
                response.type("application/json");
                return gson.toJson(new ErrorResponse("Registration failed: " + e.getMessage()));
            } catch (Exception e) {
                // Catch-all for other errors
                e.printStackTrace();
                response.status(500); // Internal Server Error
                response.type("application/json");
                return gson.toJson(new ErrorResponse("Internal server error"));
            }
        });

        // User Login endpoint
        Spark.post("/session", (request, response) -> {
            Gson gson = new Gson();
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
            try {
                LoginResult loginResult = userService.login(loginRequest);
                response.status(200); // OK
                response.type("application/json");
                return gson.toJson(loginResult);
            } catch (ServiceException e) {
                // Handle login errors such as incorrect credentials
                response.status(401); // Unauthorized
                response.type("application/json");
                return gson.toJson(new ErrorResponse("Login failed: " + e.getMessage()));
            } catch (Exception e) {
                // Catch-all for other errors
                e.printStackTrace();
                response.status(500); // Internal Server Error
                response.type("application/json");
                return gson.toJson(new ErrorResponse("Internal server error"));
            }
        });

        // Create Game endpoint
        Spark.post("/games", (request, response) -> {
            // Deserialize the request body into a CreateGameRequest object
            Gson gson = new Gson();
            CreateGameRequest createRequest = gson.fromJson(request.body(), CreateGameRequest.class);

            // Attempt to create the game through the GameService
            CreateGameResult createResponse;
            try {
                createResponse = gameService.createGame(createRequest);
            } catch (ServiceException e) {
                // Handle any errors, such as validation failures or database issues
                response.status(400); // Bad Request or another appropriate status code
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }

            // Set response type and status code
            response.type("application/json");
            response.status(201); // Created

            // Serialize and return the CreateGameResponse
            return gson.toJson(createResponse);
        });

        // Join Game endpoint
        Spark.post("/games/join", (request, response) -> {
            // Deserialize the request body into a JoinGameRequest object
            Gson gson = new Gson();
            JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);

            // Attempt to join the game through the GameService
            JoinGameResult joinResponse;
            try {
                joinResponse = gameService.joinGame(joinRequest);
            } catch (ServiceException e) {
                // Handle any errors, such as game not found or game full
                response.status(400); // Bad Request or another appropriate status code
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }

            // Check if joining was successful and set the response accordingly
            if (joinResponse.isSuccess()) {
                response.status(200); // OK
            } else {
                // You could use a more specific status code based on the failure reason
                response.status(400); // Bad Request
                return gson.toJson(new ErrorResponse(joinResponse.getMessage()));
            }

            // Set response type
            response.type("application/json");

            // Serialize and return the JoinGameResponse
            return gson.toJson(joinResponse);
        });

        // List Games endpoint
        Spark.get("/games", (request, response) -> {
            // No request body needed, just query parameters if any

            // Call GameService to get the list of games
            ListGamesResult listGamesResponse;
            try {
                listGamesResponse = gameService.listGames();
            } catch (ServiceException e) {
                // Handle any errors, such as database connection issues
                response.status(500); // Internal Server Error
                return new Gson().toJson(new ErrorResponse("Error listing games."));
            }

            // Set response type and status code
            response.type("application/json");
            response.status(200); // OK

            // Serialize and return the ListGamesResponse
            return new Gson().toJson(listGamesResponse.getGames());
        });

        // Logout endpoint
        Spark.delete("/session", (request, response) -> {
            // Extract the auth token from the request header
            String authToken = request.headers("Authorization");

            // Perform the logout operation through the UserService
            try {
                userService.logout(authToken);
            } catch (ServiceException e) {
                // Handle any errors, such as token not found or database access issues
                response.status(400); // Bad Request or another appropriate status code
                return new Gson().toJson(new ErrorResponse("Failed to log out: " + e.getMessage()));
            }

            // Set response type and status code
            response.type("application/json");
            response.status(200); // OK

            // Return a success message
            return new Gson().toJson(new SuccessResponse("Logged out successfully."));
        });

        // Clear Application endpoint
        Spark.delete("/db", (request, response) -> {
            try {
                clearService.clearDatabase();
                response.status(200); // OK
                response.type("application/json"); // Set Content-Type to application/json
                // Use Gson to convert the response object to JSON
                return new Gson().toJson(new SuccessResponse("Application data cleared successfully."));
            } catch (Exception e) {
                // Log the error and return an appropriate error response
                e.printStackTrace();
                response.status(500); // Internal Server Error
                response.type("application/json"); // Ensure the response is of type application/json
                // Use Gson to convert the error response object to JSON
                return new Gson().toJson(new ErrorResponse("Failed to clear the application data: " + e.getMessage()));
            }
        });

        // Handle exceptions here (optional)
        Spark.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
            response.status(500);
            response.body("Internal server error");
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
