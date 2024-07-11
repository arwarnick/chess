package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {
    void clear();
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}