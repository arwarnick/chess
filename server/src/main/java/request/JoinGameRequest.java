package request;

import chess.ChessGame;

public record JoinGameRequest(String playerColor, int gameID) {
    public JoinGameRequest {
        if (playerColor != null && !playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK")) {
            throw new IllegalArgumentException("Player color must be WHITE or BLACK");
        }
        if (gameID <= 0) {
            throw new IllegalArgumentException("Game ID must be a positive integer");
        }
    }

    public ChessGame.TeamColor getTeamColor() {
        if (playerColor == null) return null;
        return ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
    }
}