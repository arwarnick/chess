package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import service.GameService;
import spark.*;

public class Server {
    private final HTTPHandler httpHandler;
    private final WebSocketHandler webSocketHandler;
    private final GameService gameService;

    public Server() {
        GameDAO gameDAO = new MySqlGameDAO();
        AuthDAO authDAO = new MySqlAuthDAO();
        this.gameService = new GameService(gameDAO, authDAO);
        this.httpHandler = new HTTPHandler();
        this.webSocketHandler = new WebSocketHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        // Configure static file location
        Spark.staticFiles.location("/web");

        // Set up WebSocket endpoint
        Spark.webSocket("/ws", webSocketHandler);

        // Set up HTTP endpoints
        httpHandler.registerEndpoints();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}