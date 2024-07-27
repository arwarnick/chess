package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.ListGamesResult;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private String validAuthToken;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new MySqlGameDAO();
        authDAO = new MySqlAuthDAO();
        userDAO = new MySqlUserDAO();
        gameService = new GameService(gameDAO, authDAO);

        // Clear the database before each test
        DatabaseManager.clearDatabase();

        // First, create a test user
        userDAO.createUser(new UserData("testuser", "hashedpassword", "test@example.com"));

        // Then, create an auth token for this user
        validAuthToken = "validAuthToken";
        authDAO.createAuth(validAuthToken, "testuser");
    }

    @Test
    public void testCreateGame() throws DataAccessException {
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResult result = gameService.createGame(request, validAuthToken);

        assertNotNull(result);
        assertTrue(result.gameID() > 0);

        ListGamesResult games = gameService.listGames(validAuthToken);
        assertEquals(1, games.games().size());
        assertEquals("Test Game", games.games().get(0).gameName());
    }

    @Test
    public void testCreateGameUnauthorized() {
        CreateGameRequest request = new CreateGameRequest("Test Game");
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> gameService.createGame(request, "invalidAuthToken"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void testJoinGame() throws DataAccessException {
        // First, create a game
        CreateGameRequest createRequest = new CreateGameRequest("Test Game");
        CreateGameResult createResult = gameService.createGame(createRequest, validAuthToken);

        // Now, join the game
        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", createResult.gameID());
        assertDoesNotThrow(() -> gameService.joinGame(joinRequest, validAuthToken));

        // Verify the game state
        ListGamesResult games = gameService.listGames(validAuthToken);
        assertEquals(1, games.games().size());
        assertEquals("testuser", games.games().get(0).whiteUsername());
    }

    @Test
    public void testJoinGameUnauthorized() throws DataAccessException {
        CreateGameRequest createRequest = new CreateGameRequest("Test Game");
        CreateGameResult createResult = gameService.createGame(createRequest, validAuthToken);

        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", createResult.gameID());
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> gameService.joinGame(joinRequest, "invalidAuthToken"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void testJoinNonexistentGame() {
        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", 9999);
        
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> gameService.joinGame(joinRequest, validAuthToken));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void testListGames() throws DataAccessException {
        // Create a couple of games
        gameService.createGame(new CreateGameRequest("Game 1"), validAuthToken);
        gameService.createGame(new CreateGameRequest("Game 2"), validAuthToken);

        ListGamesResult result = gameService.listGames(validAuthToken);

        assertNotNull(result);
        assertEquals(2, result.games().size());
    }

    @Test
    public void testListGamesUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, 
            () -> gameService.listGames("invalidAuthToken"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void testClear() throws DataAccessException {
        // Create a game
        gameService.createGame(new CreateGameRequest("Test Game"), validAuthToken);

        // Verify that the game was created
        ListGamesResult beforeClear = gameService.listGames(validAuthToken);
        assertFalse(beforeClear.games().isEmpty());

        // Clear the service
        gameService.clear();

        // Verify that the auth token is no longer valid
        assertThrows(DataAccessException.class, () -> gameService.listGames(validAuthToken));

        // Recreate the user
        userDAO.createUser(new UserData("testuser", "hashedpassword", "test@example.com"));

        // Re-authenticate to get a new valid token
        authDAO.createAuth(validAuthToken, "testuser");

        // Check that the game list is empty with the new token
        ListGamesResult afterClear = gameService.listGames(validAuthToken);
        assertTrue(afterClear.games().isEmpty());
    }
}
