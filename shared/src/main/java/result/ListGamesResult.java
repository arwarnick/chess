package result;

import model.GameData;

import java.util.List;

public record ListGamesResult(List<GameData> games) {
    public ListGamesResult {
        if (games == null) {
            throw new IllegalArgumentException("Games list cannot be null");
        }
    }
}