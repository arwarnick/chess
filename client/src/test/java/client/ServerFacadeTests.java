package client;

import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    void clearPositive() throws Exception {
        // First, add some data to the server
        facade.register("testUser", "password", "test@email.com");
        facade.createGame("testGame", facade.login("testUser", "password").authToken());

        // Now clear the database
        assertDoesNotThrow(() -> facade.clear());

        // Verify that the database is empty by trying to log in with the previously created user
        assertThrows(Exception.class, () -> facade.login("testUser", "password"));
    }

    @Test
    void clearNegative() {
        // The clear method doesn't have a true negative case as it should always succeed
        // However, we can test that calling clear multiple times doesn't cause issues
        assertDoesNotThrow(() -> {
            facade.clear();
            facade.clear(); // Second call should not throw an exception
        });
    }

    @Test
    void registerPositive() throws Exception {
        var result = facade.register("player1", "password", "p1@email.com");
        assertNotNull(result.authToken());
        assertEquals("player1", result.username());
    }

    @Test
    void registerNegative() {
        assertThrows(Exception.class, () -> {
            facade.register("player1", "password", "p1@email.com");
            facade.register("player1", "password", "p1@email.com");
        });
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        var result = facade.login("player1", "password");
        assertNotNull(result.authToken());
        assertEquals("player1", result.username());
    }

    @Test
    void loginNegative() {
        assertThrows(Exception.class, () -> facade.login("nonexistent", "wrongpassword"));
    }

    @Test
    void logoutPositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutNegative() {
        assertThrows(Exception.class, () -> facade.logout("invalidAuthToken"));
    }

    @Test
    void createGamePositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        var result = facade.createGame("testGame", auth.authToken());
        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("testGame", "invalidAuthToken"));
    }

    @Test
    void listGamesPositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        facade.createGame("testGame1", auth.authToken());
        facade.createGame("testGame2", auth.authToken());
        var result = facade.listGames(auth.authToken());
        assertNotNull(result);
        assertEquals(2, result.games().size());
    }

    @Test
    void listGamesNegative() {
        assertThrows(Exception.class, () -> facade.listGames("invalidAuthToken"));
    }

    @Test
    void joinGamePositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        var gameResult = facade.createGame("testGame", auth.authToken());
        assertDoesNotThrow(() -> facade.joinGame("WHITE", gameResult.gameID(), auth.authToken()));
    }

    @Test
    void joinGameNegative() {
        assertThrows(Exception.class, () -> facade.joinGame("WHITE", 9999, "invalidAuthToken"));
    }

    @Test
    void getGamePositive() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        var gameResult = facade.createGame("testGame", auth.authToken());

        var game = facade.getGame(gameResult.gameID(), auth.authToken());

        assertNotNull(game, "Game should not be null");
        assertEquals("testGame", game.gameName(), "Game name should match the created game name");
    }

    @Test
    void getGameNegative() throws Exception {
        var auth = facade.register("player1", "password", "p1@email.com");
        assertThrows(Exception.class, () -> facade.getGame(9999, auth.authToken()));
    }
}