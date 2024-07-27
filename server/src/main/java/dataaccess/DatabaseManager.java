package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    public static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public static void createTables() throws DataAccessException {
        try (Connection conn = getConnection()) {
            // Create users table
            String usersSql = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
            """;
            try (PreparedStatement stmt = conn.prepareStatement(usersSql)) {
                stmt.executeUpdate();
            }

            // Create auth_tokens table
            String authTokensSql = """
            CREATE TABLE IF NOT EXISTS auth_tokens (
                auth_token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
            )
            """;
            try (PreparedStatement stmt = conn.prepareStatement(authTokensSql)) {
                stmt.executeUpdate();
            }

            // Check if the foreign key constraint already exists
            if (!constraintExists(conn, "auth_tokens", "auth_tokens_ibfk_1")) {
                // Add foreign key constraint to auth_tokens
                String alterAuthTokensSql = """
                ALTER TABLE auth_tokens
                ADD CONSTRAINT auth_tokens_ibfk_1
                FOREIGN KEY (username) REFERENCES users(username)
                ON DELETE CASCADE
                """;
                try (PreparedStatement stmt = conn.prepareStatement(alterAuthTokensSql)) {
                    stmt.executeUpdate();
                }
            }

            // Create games table
            String gamesSql = """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT PRIMARY KEY AUTO_INCREMENT,
                white_username VARCHAR(255),
                black_username VARCHAR(255),
                game_name VARCHAR(255) NOT NULL,
                game_state TEXT,
                FOREIGN KEY (white_username) REFERENCES users(username),
                FOREIGN KEY (black_username) REFERENCES users(username)
            )
            """;
            try (PreparedStatement stmt = conn.prepareStatement(gamesSql)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables: " + e.getMessage());
        }
    }

    private static boolean constraintExists(Connection conn, String tableName, String constraintName) throws SQLException {
        String checkConstraintSql = """
            SELECT COUNT(*)
            FROM information_schema.table_constraints
            WHERE table_schema = DATABASE()
              AND table_name = ?
              AND constraint_name = ?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(checkConstraintSql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, constraintName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void clearDatabase() throws DataAccessException {
        try (Connection conn = getConnection()) {
            String[] tables = {"auth_tokens", "games", "users"};
            for (String table : tables) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing database: " + e.getMessage());
        }
    }
}
