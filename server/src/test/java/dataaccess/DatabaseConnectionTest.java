package dataaccess;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    @BeforeAll
    public static void setUp() {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
        } catch (DataAccessException e) {
            System.err.println("Error setting up database: " + e.getMessage());
        }
    }

    @Test
    public void testDatabaseConnection() {
        try (Connection conn = DatabaseManager.getConnection()) {
            Assertions.assertNotNull(conn, "Connection should not be null");
            Assertions.assertFalse(conn.isClosed(), "Connection should be open");
        } catch (SQLException | DataAccessException e) {
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testUserTableExists() {
        try (Connection conn = DatabaseManager.getConnection()) {
            var metadata = conn.getMetaData();
            var resultSet = metadata.getTables(null, null, "users", null);
            Assertions.assertTrue(resultSet.next(), "users table should exist");
        } catch (SQLException | DataAccessException e) {
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGameTableExists() {
        try (Connection conn = DatabaseManager.getConnection()) {
            var metadata = conn.getMetaData();
            var resultSet = metadata.getTables(null, null, "games", null);
            Assertions.assertTrue(resultSet.next(), "games table should exist");
        } catch (SQLException | DataAccessException e) {
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testAuthTokenTableExists() {
        try (Connection conn = DatabaseManager.getConnection()) {
            var metadata = conn.getMetaData();
            var resultSet = metadata.getTables(null, null, "auth_tokens", null);
            Assertions.assertTrue(resultSet.next(), "auth_tokens table should exist");
        } catch (SQLException | DataAccessException e) {
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }
}