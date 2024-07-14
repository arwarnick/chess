package dataaccess;

import model.GameData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of the GameDAO interface.
 * This class stores game data in a HashMap and manages game IDs.
 */
public class MemoryGameDAO implements GameDAO {
    /** Map to store game IDs and their associated game data */
    private final Map<Integer, GameData> games = new HashMap<>();
    /** Counter for generating unique game IDs */
    private int nextGameId = 1;

    @Override
    public void clear() {
        games.clear();
        nextGameId = 1;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        int gameId = nextGameId++;
        games.put(gameId, new GameData(gameId, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found");
        }
        games.put(game.gameID(), game);
    }
}