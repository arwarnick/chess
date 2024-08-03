package client;

import com.google.gson.Gson;
import model.GameData;
import request.*;
import result.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final String serverUrl;
    private final HttpClient client;
    private final Gson gson;

    public ServerFacade(String url) {
        serverUrl = url;
        client = HttpClient.newHttpClient();
        gson = new Gson();
    }

    public void clear() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/db"))
                .DELETE()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
    }

    public RegisterResult register(String username, String password, String email) throws Exception {
        var registerRequest = new RegisterRequest(username, password, email);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(registerRequest)))
                .header("Content-Type", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
        return gson.fromJson(response.body(), RegisterResult.class);
    }

    public LoginResult login(String username, String password) throws Exception {
        var loginRequest = new LoginRequest(username, password);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(loginRequest)))
                .header("Content-Type", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
        return gson.fromJson(response.body(), LoginResult.class);
    }

    public void logout(String authToken) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .DELETE()
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .GET()
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
        return gson.fromJson(response.body(), ListGamesResult.class);
    }

    public CreateGameResult createGame(String gameName, String authToken) throws Exception {
        var createGameRequest = new CreateGameRequest(gameName);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(createGameRequest)))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
        return gson.fromJson(response.body(), CreateGameResult.class);
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws Exception {
        var joinGameRequest = new JoinGameRequest(playerColor, gameID);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(joinGameRequest)))
                .header("Content-Type", "application/json")
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        handleResponse(response);
    }

    public GameData getGame(int gameId, String authToken) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game?id=" + gameId))
                .GET()
                .header("Authorization", authToken)
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        handleResponse(response);

        // Parse the response as a ListGamesResult
        ListGamesResult listResult = gson.fromJson(response.body(), ListGamesResult.class);

        // Find the game with the matching ID
        GameData gameData = listResult.games().stream()
                .filter(game -> game.gameID() == gameId)
                .findFirst()
                .orElseThrow(() -> new Exception("Game not found"));

        return gameData;
    }

    private void handleResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            throw new Exception(gson.fromJson(response.body(), ErrorResult.class).message());
        }
    }
}