import java.sql.*;

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
                "userID INTEGER PRIMARY KEY," +
                "userName TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "isPremium INTEGER NOT NULL" +
                ");";

        String createUserActionsTable = "CREATE TABLE IF NOT EXISTS UserActions (" +
                "actionID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userID INTEGER NOT NULL," +
                "articleID INTEGER NOT NULL," +
                "action TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(userID) REFERENCES User(userID)," +
                "FOREIGN KEY(articleID) REFERENCES Articles(articleID)" +
                ");";

        String createArticlesTable = "CREATE TABLE IF NOT EXISTS Articles (" +
                "articleID INTEGER PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "articlePath TEXT NOT NULL," +
                "createdAt DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createScoresTable = "CREATE TABLE IF NOT EXISTS Scores (" +
                "userID INTEGER NOT NULL," +
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

    // For register method --- userAccountServices class
    public static boolean doesUserNameExist(String userName) {
        String query = "SELECT userName FROM User WHERE userName = ?";
        boolean exists = false;

        try (Connection conn = connect();
             PreparedStatement preStmt = conn.prepareStatement(query)) {

            preStmt.setString(1, userName);
            ResultSet rs = preStmt.executeQuery();

            exists = rs.next();

        } catch (SQLException e) {
            System.out.println("Error occurred while checking userName: " + e.getMessage());
        }

        return exists;
    }

    // For register method --- userAccountServices class
    public static int assignUserID() {
        String query = "SELECT MAX(userID) FROM User;";
        int newUserID = 1; // Default starting userID

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int lastUserID = rs.getInt(1); // Get the MAX(userID)
                if (!rs.wasNull()) {          // Check if the result was NULL
                    newUserID = lastUserID + 1;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error assigning userID: " + e.getMessage());
        }

        return newUserID;
    }

    // For register method --- userAccountServices class
    public static void addNewUser(String userName, String password){
        int userId = assignUserID();
        String insertUser = "INSERT INTO User (userID, userName, password, isPremium) VALUES (?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertUser)) {

            pstmt.setInt(1, userId);        // Auto-assigned userID
            pstmt.setString(2, userName);  // Provided userName
            pstmt.setString(3, password);  // Provided password
            pstmt.setInt(4, 0);            // Default isPremium value

            pstmt.executeUpdate();
            System.out.println("New user added successfully with userID: " + userId);

        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }


    // For goPremium method --- userAccountServices class
    public static void goPremium(String userName){
        String updateIsPremium = "UPDATE User SET isPremium = 1 WHERE userName = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(updateIsPremium)) {

            pstmt.setString(1, userName); // Set the userName parameter in the query
            int rowsUpdated = pstmt.executeUpdate(); // Execute the update query

            if (rowsUpdated > 0) {
                System.out.println("User " + userName + " has been updated to Premium.");
            } else {
                System.out.println("User " + userName + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating user to Premium: " + e.getMessage());
        }
    }

    // For login method --- userAccountServices class
    public static boolean passwordMatching(String userName, String password) {
        // Query to fetch the password for the given userName
        String query = "SELECT password FROM User WHERE userName = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the userName parameter
            pstmt.setString(1, userName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Get the password from the result set
                    String storedPassword = rs.getString("password");
                    // Check if the provided password matches the stored password
                    return storedPassword.equals(password);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking password: " + e.getMessage());
        }
        return false;
    }

    // For deleteAccount method --- userAccountServices class
    public static void removeUser(String userName) {
        // Query to find userID based on userName
        String findUserIDQuery = "SELECT userID FROM User WHERE userName = ?;";

        // Queries to delete user and related entries
        String deleteUserQuery = "DELETE FROM User WHERE userID = ?;";
        String deleteUserActionsQuery = "DELETE FROM UserActions WHERE userID = ?;";
        String deleteScoresQuery = "DELETE FROM Scores WHERE userID = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement findUserIDStmt = conn.prepareStatement(findUserIDQuery)) {

            // Find the userID based on userName
            findUserIDStmt.setString(1, userName);
            ResultSet rs = findUserIDStmt.executeQuery();

            if (rs.next()) {
                String userID = rs.getString("userID");

                // Delete entries in a transaction to maintain data integrity
                conn.setAutoCommit(false);

                try (PreparedStatement deleteUserActionsStmt = conn.prepareStatement(deleteUserActionsQuery);
                     PreparedStatement deleteScoresStmt = conn.prepareStatement(deleteScoresQuery);
                     PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserQuery)) {

                    // Delete entries from UserActions
                    deleteUserActionsStmt.setString(1, userID);
                    deleteUserActionsStmt.executeUpdate();

                    // Delete entries from Scores
                    deleteScoresStmt.setString(1, userID);
                    deleteScoresStmt.executeUpdate();

                    // Delete the user from User table
                    deleteUserStmt.setString(1, userID);
                    deleteUserStmt.executeUpdate();

                    // Commit the transaction
                    conn.commit();
                    System.out.println("User and related entries removed successfully.");

                } catch (SQLException e) {
                    // Rollback if any operation fails
                    conn.rollback();
                    System.out.println("Error removing user: " + e.getMessage());
                } finally {
                    // Restore auto-commit mode
                    conn.setAutoCommit(true);
                }
            } else {
                System.out.println("User not found with the provided userName.");
            }

        } catch (SQLException e) {
            System.out.println("Error removing user: " + e.getMessage());
        }
    }


}