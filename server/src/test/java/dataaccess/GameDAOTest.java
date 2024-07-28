package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDAOTest {
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        // Positive test
        GameData game = new GameData(0, null, null, "Test Game", new ChessGame());
        gameDAO.createGame(game);

        List<GameData> games = gameDAO.listGames();
        assertFalse(games.isEmpty());
        GameData createdGame = games.stream()
                .filter(g -> g.gameName().equals("Test Game"))
                .findFirst()
                .orElse(null);
        assertNotNull(createdGame);
        assertEquals(game.gameName(), createdGame.gameName());
    }

    @Test
    void createGameInvalidData() {
        // Negative test
        GameData invalidGame = new GameData(0, "nonexistentuser", null, null, null);
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(invalidGame));
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        // Positive test
        GameData game = new GameData(0, null, null, "Test Game", new ChessGame());
        gameDAO.createGame(game);

        List<GameData> games = gameDAO.listGames();
        int gameId = games.get(0).gameID();

        GameData retrievedGame = gameDAO.getGame(gameId);
        assertNotNull(retrievedGame);
        assertEquals(game.gameName(), retrievedGame.gameName());
    }

    @Test
    void getGameNonExistent() throws DataAccessException {
        // Negative test
        GameData retrievedGame = gameDAO.getGame(9999);
        assertNull(retrievedGame);
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        // Positive test
        GameData game1 = new GameData(0, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(0, null, null, "Test Game 2", new ChessGame());
        gameDAO.createGame(game1);
        gameDAO.createGame(game2);

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGamesEmpty() throws DataAccessException {
        // Negative test (or edge case)
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        // Positive test
        UserDAO userDAO = new MySqlUserDAO();
        // Create test users
        userDAO.createUser(new UserData("white", "password", "white@example.com"));
        userDAO.createUser(new UserData("black", "password", "black@example.com"));

        GameData game = new GameData(0, null, null, "Test Game", new ChessGame());
        gameDAO.createGame(game);

        List<GameData> games = gameDAO.listGames();
        GameData createdGame = games.get(0);

        GameData updatedGame = new GameData(createdGame.gameID(), "white", "black", "Updated Game", new ChessGame());
        gameDAO.updateGame(updatedGame);

        GameData retrievedGame = gameDAO.getGame(createdGame.gameID());
        assertNotNull(retrievedGame);
        assertEquals("Updated Game", retrievedGame.gameName());
        assertEquals("white", retrievedGame.whiteUsername());
        assertEquals("black", retrievedGame.blackUsername());
    }

    @Test
    void updateGameNonExistent() throws DataAccessException {
        // Negative test
        GameData nonExistentGame = new GameData(9999, "white", "black", "Non-existent Game", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(nonExistentGame));
    }

    @Test
    void clearSuccess() throws DataAccessException {
        // Positive test
        GameData game = new GameData(0, null, null, "Test Game", new ChessGame());
        gameDAO.createGame(game);

        gameDAO.clear();
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }
}