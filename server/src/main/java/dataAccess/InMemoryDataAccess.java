package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDataAccess implements IDataAccess {
    private Map<String, UserData> users = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();
    private Map<String, AuthData> authTokens = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if(users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        if(user == null) {
            throw new DataAccessException("User not found");
        }
        return user;
    }

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
        if(!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found to update");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = authTokens.get(authToken);
        if(auth == null) {
            throw new DataAccessException("Auth token not found");
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if(!authTokens.containsKey(authToken)) {
            throw new DataAccessException("Auth token not found to delete");
        }
        authTokens.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        games.clear();
        authTokens.clear();
        nextGameId = 1; // Reset the game ID counter
    }
}
