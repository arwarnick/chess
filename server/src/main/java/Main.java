import chess.*;
import server.Server;
import service.*;
import dataAccess.*;

public class Main {
    public static void main(String[] args) {
        // Instantiate the concrete DAO implementations
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        AuthDAO authDAO = new MemoryAuthDAO();

        // Instantiate the services with the DAO implementations
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

        // Create a server instance with the services
        Server server = new Server(userService, gameService, clearService);

        // Start the server on a specified port
        int port = 8080; // Change this to your desired port
        server.run(port);

        System.out.println("♕ 240 Chess Server is running on port " + port);
    }
}
