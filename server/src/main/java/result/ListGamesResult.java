package result;

import model.GameData;

import java.util.List;

public class ListGamesResult {
    private final List<GameInfo> games;

    public ListGamesResult(List<GameData> games) {
        this.games = games;
    }

    public List<GameInfo> getGames() {
        return games;
    }

    // Inner class to represent individual game information
    public static class GameInfo {
        private final int gameID;
        private final String whiteUsername;
        private final String blackUsername;
        private final String gameName;

        public GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) {
            this.gameID = gameID;
            this.whiteUsername = whiteUsername;
            this.blackUsername = blackUsername;
            this.gameName = gameName;
        }

        // Getters
        public int getGameID() {
            return gameID;
        }

        public String getWhiteUsername() {
            return whiteUsername;
        }

        public String getBlackUsername() {
            return blackUsername;
        }

        public String getGameName() {
            return gameName;
        }
    }
}

