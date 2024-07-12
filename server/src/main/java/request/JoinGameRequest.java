package request;

import chess.ChessGame;

public record JoinGameRequest(String playerColor, int gameID) {
    // Remove the constructor validation

    public ChessGame.TeamColor getTeamColor() {
        if (playerColor == null) return null;
        return ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
    }
}