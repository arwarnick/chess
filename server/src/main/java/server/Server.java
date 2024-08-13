package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import result.ErrorResult;
import service.GameService;
import spark.*;

public class Server {
    private final HTTPHandler httpHandler;
    private final WebSocketHandler webSocketHandler;
    private final GameService gameService;
    private final Gson gson;  // Add this line

    public Server() {
        GameDAO gameDAO = new MySqlGameDAO();
        AuthDAO authDAO = new MySqlAuthDAO();
        this.gameService = new GameService(gameDAO, authDAO);
        this.httpHandler = new HTTPHandler();
        this.webSocketHandler = new WebSocketHandler(gameService);
        this.gson = new Gson();  // Add this line
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        // Configure static file location
        Spark.staticFiles.location("/web");

        // Set up WebSocket endpoint
        Spark.webSocket("/ws", webSocketHandler);

        // Set up HTTP endpoints
        httpHandler.registerEndpoints();

        // Global exception handler
        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(new ErrorResult("Internal server error")));
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}