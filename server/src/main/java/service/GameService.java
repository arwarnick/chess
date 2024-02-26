package service;

import chess.ChessGame;
import dataAccess.GameDAO;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.JoinGameResult;
import result.ListGamesResult;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames() throws ServiceException {
        try {
            List<GameData> games = gameDAO.listGames();
            return new ListGamesResult(games);
        } catch (dataAccess.DataAccessException e) {
            throw new ServiceException("Failed to list games: " + e.getMessage(), e);
        }
    }

    public CreateGameResult createGame(CreateGameRequest request) throws ServiceException {
        try {
            // Create a new game with the provided name and an empty state
            GameData newGame = new GameData(0, null, null, request.getGameName(), new ChessGame());
            gameDAO.createGame(newGame);

            // Return the create game response with the new game ID
            return new CreateGameResult(newGame.gameID());
        } catch (dataAccess.DataAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws ServiceException {
        try {
            GameData game = gameDAO.getGame(request.getGameID());
            if (game == null) {
                throw new ServiceException("Game not found.");
            }

            // Business logic to join the game would go here.
            // This may involve updating the game state and re-saving it with the DAO.

            boolean success = false;
            String message = null;
            return new JoinGameResult(success, null);
        } catch (dataAccess.DataAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}

