package dataAccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public void createGame(GameData game) throws DataAccessException {
        GameData newGame = new GameData(nextGameId++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(newGame.gameID(), newGame);
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        GameData game = games.get(gameId);
        if(game == null) {
            throw new DataAccessException("Game not found");
        }
        return game;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        games.put(game.gameID(), game); // In-memory, we replace the game.
    }

    @Override
    public void clearGames() {
        games.clear();
        nextGameId = 1; // Reset the game ID counter if you're incrementing game IDs
    }
}
