package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
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
    private String validAuthToken;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        // Add a valid auth token
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

        // Clear the service
        gameService.clear();

        // Check that the game list is empty
        ListGamesResult result = gameService.listGames(validAuthToken);
        assertTrue(result.games().isEmpty());
    }
}
