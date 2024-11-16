import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    private static final String URL = "jdbc:sqlite:news_recommendation_system.db";

    // Method to create a database connection
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Error occurred while connecting to the database.");
            e.printStackTrace();
        }
        return conn;
    }

    // Method to close the connection safely
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // method for creating tables
    public static void createTables() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS User (" +
                "userID TEXT PRIMARY KEY," +
                "userName TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "isPremium INTEGER NOT NULL" +
                ");";

        String createUserActionsTable = "CREATE TABLE IF NOT EXISTS UserActions (" +
                "actionID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userID TEXT NOT NULL," +
                "articleID TEXT NOT NULL," +
                "action TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(userID) REFERENCES User(userID)," +
                "FOREIGN KEY(articleID) REFERENCES Articles(articleID)" +
                ");";

        String createArticlesTable = "CREATE TABLE IF NOT EXISTS Articles (" +
                "articleID TEXT PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "articlePath TEXT NOT NULL," +
                "createdAt DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createScoresTable = "CREATE TABLE IF NOT EXISTS Scores (" +
                "userID TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "score INTEGER DEFAULT 0," +
                "PRIMARY KEY (userID, category)," +
                "FOREIGN KEY(userID) REFERENCES User(userID)" +
                ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createUserActionsTable);
            stmt.execute(createArticlesTable);
            stmt.execute(createScoresTable);
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

}