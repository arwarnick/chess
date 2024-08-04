package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.ListGamesResult;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData newGame = new GameData(0, null, null, request.gameName(), new ChessGame());
        gameDAO.createGame(newGame);

        // Retrieve the created game to get its ID
        List<GameData> games = gameDAO.listGames();
        GameData createdGame = games.stream()
                .filter(game -> game.gameName().equals(request.gameName()))
                .findFirst()
                .orElseThrow(() -> new DataAccessException("Error: failed to create game"));

        return new CreateGameResult(createdGame.gameID());
    }

    public void joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (request.CheckIfObserver()) {
            return;
        }

        ChessGame.TeamColor color = request.getTeamColor();
        if (color == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (color == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }
        if (color == ChessGame.TeamColor.BLACK && game.blackUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                color == ChessGame.TeamColor.WHITE ? authData.username() : game.whiteUsername(),
                color == ChessGame.TeamColor.BLACK ? authData.username() : game.blackUsername(),
                game.gameName(),
                game.game()
        );
        gameDAO.updateGame(updatedGame);
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        List<GameData> games = gameDAO.listGames();
        return new ListGamesResult(games);
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }
}