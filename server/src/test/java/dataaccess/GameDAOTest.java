package dataaccess;

import chess.ChessGame;
import model.GameData;
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
    void createGame_success() throws DataAccessException {
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
    void createGame_duplicate() throws DataAccessException {
        // Negative test
        GameData game1 = new GameData(0, null, null, "Test Game", new ChessGame());
        gameDAO.createGame(game1);

        GameData game2 = new GameData(0, null, null, "Test Game", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(game2));
    }

    @Test
    void getGame_success() throws DataAccessException {
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
    void getGame_nonExistent() throws DataAccessException {
        // Negative test
        GameData retrievedGame = gameDAO.getGame(9999);
        assertNull(retrievedGame);
    }

    @Test
    void listGames_success() throws DataAccessException {
        // Positive test
        GameData game1 = new GameData(0, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(0, null, null, "Test Game 2", new ChessGame());
        gameDAO.createGame(game1);
        gameDAO.createGame(game2);

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGames_empty() throws DataAccessException {
        // Negative test (or edge case)
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGame_success() throws DataAccessException {
        // Positive test
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
    void updateGame_nonExistent() throws DataAccessException {
        // Negative test
        GameData nonExistentGame = new GameData(9999, "white", "black", "Non-existent Game", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(nonExistentGame));
    }

    @Test
    void clear_success() throws DataAccessException {
        // Positive test
        GameData game = new GameData(0, null, null, "Test Game", new ChessGame());
        gameDAO.createGame(game);

        gameDAO.clear();
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }
}