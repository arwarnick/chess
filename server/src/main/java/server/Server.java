package server;

import spark.*;

public class Server {
    private final HTTPHandler httpHandler;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        httpHandler = new HTTPHandler();
        webSocketHandler = new WebSocketHandler();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        // Configure static file location
        Spark.staticFiles.location("/web");

        // Set up HTTP endpoints
        httpHandler.registerEndpoints();

        // Set up WebSocket endpoint
        Spark.webSocket("/ws", webSocketHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}