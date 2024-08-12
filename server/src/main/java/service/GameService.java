package service;

import chess.ChessGame;
import chess.ChessMove;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.ListGamesResult;

import java.util.List;
import java.util.Objects;

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

        if (request.checkIfObserver()) {
            return;
        }

        ChessGame.TeamColor color = request.getTeamColor();
        if (color == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if the requested color is available
        if ((color == ChessGame.TeamColor.WHITE && game.whiteUsername() == null) ||
                (color == ChessGame.TeamColor.BLACK && game.blackUsername() == null)) {
            GameData updatedGame = new GameData(
                    game.gameID(),
                    color == ChessGame.TeamColor.WHITE ? authData.username() : game.whiteUsername(),
                    color == ChessGame.TeamColor.BLACK ? authData.username() : game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
            gameDAO.updateGame(updatedGame);
        } else {
            throw new DataAccessException("Error: already taken");
        }
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

    // New methods to support WebSocket functionality

    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.getGame(gameID);
    }

    public String getUsernameFromAuthToken(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return authData.username();
    }

    public GameData makeMove(int gameID, String authToken, ChessMove move) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        // Check if it's the player's turn
        ChessGame.TeamColor currentTurn = game.game().getTeamTurn();
        boolean isWhiteTurn = currentTurn == ChessGame.TeamColor.WHITE;
        if ((isWhiteTurn && !Objects.equals(game.whiteUsername(), authData.username())) ||
                (!isWhiteTurn && !Objects.equals(game.blackUsername(), authData.username()))) {
            throw new DataAccessException("Error: not your turn");
        }

        // Make the move
        try {
            game.game().makeMove(move);
        } catch (chess.InvalidMoveException e) {
            throw new DataAccessException("Error: invalid move");
        }

        // Update the game in the database
        gameDAO.updateGame(game);

        return game;
    }

    public void resignGame(int gameID, String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        // Check if the game is already over
        if (game.game().isInCheckmate(ChessGame.TeamColor.WHITE) ||
                game.game().isInCheckmate(ChessGame.TeamColor.BLACK) ||
                game.game().isInStalemate(ChessGame.TeamColor.WHITE) ||
                game.game().isInStalemate(ChessGame.TeamColor.BLACK)) {
            throw new DataAccessException("Error: game is already over");
        }

        // Check if the player is part of the game
        if (!Objects.equals(game.whiteUsername(), authData.username()) &&
                !Objects.equals(game.blackUsername(), authData.username())) {
            throw new DataAccessException("Error: you are not a player in this game");
        }

        // Update the game state to indicate resignation
        // This could be done by setting a "resigned" flag in the ChessGame class
        // or by updating the GameData to include a resignation status
        // For now, we'll just remove the resigning player from the game
        if (Objects.equals(game.whiteUsername(), authData.username())) {
            game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
        } else {
            game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
        }

        gameDAO.updateGame(game);
    }
}