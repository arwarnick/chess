package request;

import chess.ChessGame;

public record JoinGameRequest(String playerColor, int gameID) {

    public ChessGame.TeamColor getTeamColor() {
        if (playerColor == null) {
            return null;
        }
        return ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
    }

    public boolean checkIfObserver() {
        if (playerColor == null) {
            return false;
        }
        if (playerColor.equals("observer")) {
            return true;
        }
        return false;
    }
}