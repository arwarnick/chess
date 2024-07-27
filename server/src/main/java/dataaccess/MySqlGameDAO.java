package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {
        DatabaseManager.clearDatabase();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO games (white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, gson.toJson(game.game()));
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int gameId = generatedKeys.getInt(1);
                        // You might want to do something with the generated gameId
                    } else {
                        throw new DataAccessException("Creating game failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM games WHERE game_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, gameID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("game_id"),
                                rs.getString("white_username"),
                                rs.getString("black_username"),
                                rs.getString("game_name"),
                                gson.fromJson(rs.getString("game_state"), ChessGame.class)
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM games";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        games.add(new GameData(
                                rs.getInt("game_id"),
                                rs.getString("white_username"),
                                rs.getString("black_username"),
                                rs.getString("game_name"),
                                gson.fromJson(rs.getString("game_state"), ChessGame.class)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, gson.toJson(game.game()));
                stmt.setInt(5, game.gameID());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Updating game failed, no rows affected.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }
}