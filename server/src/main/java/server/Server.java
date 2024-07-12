package server;

import com.google.gson.Gson;
import spark.*;
import service.*;
import dataaccess.*;
import request.*;
import result.*;

public class Server {
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;
    private final Gson gson = new Gson();

    public Server() {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        this.userService = new UserService(userDAO, authDAO);
        this.authService = new AuthService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", this::handleClear);
        Spark.post("/user", this::handleRegister);
        Spark.post("/session", this::handleLogin);
        Spark.delete("/session", this::handleLogout);
        Spark.get("/game", this::handleListGames);
        Spark.post("/game", this::handleCreateGame);
        Spark.put("/game", this::handleJoinGame);

        Spark.exception(DataAccessException.class, this::handleException);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object handleClear(Request req, Response res) {
        userService.clear();
        authService.clear();
        gameService.clear();
        res.status(200);
        return "{}";
    }

    private Object handleRegister(Request req, Response res) {
        var registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        try {
            RegisterResult result = userService.register(registerRequest);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Error: bad request")) {
                res.status(400);
            } else if (e.getMessage().equals("Error: already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
            return gson.toJson(new ErrorResult(e.getMessage()));
        }
    }

    private Object handleLogin(Request req, Response res) {
        var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        try {
            LoginResult result = authService.login(loginRequest);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult(e.getMessage()));
        }
    }

    private Object handleLogout(Request req, Response res) {
        String authToken = req.headers("Authorization");
        try {
            authService.logout(authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult(e.getMessage()));
        }
    }

    private Object handleListGames(Request req, Response res) {
        String authToken = req.headers("Authorization");
        try {
            ListGamesResult result = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult(e.getMessage()));
        }
    }

    private Object handleCreateGame(Request req, Response res) {
        String authToken = req.headers("Authorization");
        var createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
        try {
            CreateGameResult result = gameService.createGame(createGameRequest, authToken);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(new ErrorResult(e.getMessage()));
        }
    }

    private Object handleJoinGame(Request req, Response res) {
        String authToken = req.headers("Authorization");
        var joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
        try {
            gameService.joinGame(joinGameRequest, authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            if (e.getMessage().contains("bad request")) {
                res.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else if (e.getMessage().contains("already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
            return gson.toJson(new ErrorResult(e.getMessage()));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(new ErrorResult("Error: bad request"));
        }
    }

    private void handleException(Exception e, Request req, Response res) {
        res.status(500);
        res.body(gson.toJson(new ErrorResult(e.getMessage())));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}