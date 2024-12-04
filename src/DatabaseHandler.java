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
                "isAdmin INTEGER NOT NULL" +
                ");";

        String createUserActionsTable = "CREATE TABLE IF NOT EXISTS UserActions (" +
                "userID INTEGER NOT NULL," +
                "articleID INTEGER NOT NULL," +
                "action TEXT NOT NULL CHECK (action IN ('like', 'dislike', 'view'))," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (userID, articleID, action)," +
                "FOREIGN KEY(userID) REFERENCES User(userID)," +
                "FOREIGN KEY(articleID) REFERENCES Articles(articleID)" +
                ");";

        String createArticlesTable = "CREATE TABLE IF NOT EXISTS Articles (" +
                "articleID INTEGER PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "articlePath TEXT NOT NULL," +
                "publishDate DATE NOT NULL" +
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

    public static boolean isAdminUser(String userName) {
        String query = "SELECT isAdmin FROM User WHERE userName = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the userName parameter
            pstmt.setString(1, userName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Check if the isAdmin field is 1
                    return rs.getInt("isAdmin") == 1;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking admin status: " + e.getMessage());
        }

        return false; // Return false if userName not found or error occurs
    }



    // For deleteAccount method --- userAccountServices class
    public static void logOutUser(String userName) {
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

    public static int assignArticleID(){
        String query = "SELECT MAX(ArticleID) FROM Article;";
        int newArticleID = 1; // Default starting userID

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int lastArticleID = rs.getInt(1); // Get the MAX(userID)
                if (!rs.wasNull()) {          // Check if the result was NULL
                    newArticleID = lastArticleID + 1;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error assigning ArticleOD: " + e.getMessage());
        }

        return newArticleID;
    }

    // For storeInDatabase method --- Article class
    public static void storeAnArticle(String title, String content, String category, String path) {
        int articleID = assignArticleID(); // Generate the articleID

        // SQL query to insert a new article into the Articles table
        String insertArticleQuery = "INSERT INTO Articles (articleID, title, content, category, articlePath) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertArticleQuery)) {

            // Set the values for the placeholders
            pstmt.setInt(1, articleID); // Set articleID
            pstmt.setString(2, title);  // Set title
            pstmt.setString(3, content); // Set content
            pstmt.setString(4, category); // Set category
            pstmt.setString(5, path); // Set articlePath

            // Execute the insert query
            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Article stored successfully with articleID: " + articleID);
            } else {
                System.out.println("Failed to store the article.");
            }
        } catch (SQLException e) {
            System.out.println("Error storing the article: " + e.getMessage());
        }
    }

    // For viewArticles method in UserActions class
    public static void addViewUserAction(String userName, String title) {
        // Query to get userID based on userName
        String getUserIDQuery = "SELECT userID FROM User WHERE userName = ?";

        // Query to get articleID based on title
        String getArticleIDQuery = "SELECT articleID FROM Articles WHERE title = ?";

        // Query to insert into UserActions
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action) VALUES (?, ?, 'view')";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Enable transaction management

            // Retrieve userID
            int userID = -1;
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIDQuery)) {
                pstmtUser.setString(1, userName);
                ResultSet rsUser = pstmtUser.executeQuery();
                if (rsUser.next()) {
                    userID = rsUser.getInt("userID");
                } else {
                    System.out.println("User not found: " + userName);
                    return;
                }
            }

            // Retrieve articleID
            int articleID = -1;
            try (PreparedStatement pstmtArticle = conn.prepareStatement(getArticleIDQuery)) {
                pstmtArticle.setString(1, title);
                ResultSet rsArticle = pstmtArticle.executeQuery();
                if (rsArticle.next()) {
                    articleID = rsArticle.getInt("articleID");
                } else {
                    System.out.println("Article not found: " + title);
                    return;
                }
            }

            // Insert into UserActions
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertUserActionQuery)) {
                pstmtInsert.setInt(1, userID);
                pstmtInsert.setInt(2, articleID);
                int rowsInserted = pstmtInsert.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("View action added successfully for user: " + userName);
                } else {
                    System.out.println("Failed to add view action.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.out.println("Error adding view action: " + e.getMessage());
        }
    }

    // For likeArticle method -- UserActions class
    public static void addLikeUserAction(String userName, String title) {
        // Query to get userID based on userName
        String getUserIDQuery = "SELECT userID FROM User WHERE userName = ?";

        // Query to get articleID based on title
        String getArticleIDQuery = "SELECT articleID FROM Articles WHERE title = ?";

        // Query to insert into UserActions
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action) VALUES (?, ?, 'like')";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Enable transaction management

            // Retrieve userID
            int userID = -1;
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIDQuery)) {
                pstmtUser.setString(1, userName);
                ResultSet rsUser = pstmtUser.executeQuery();
                if (rsUser.next()) {
                    userID = rsUser.getInt("userID");
                } else {
                    System.out.println("User not found: " + userName);
                    return;
                }
            }

            // Retrieve articleID
            int articleID = -1;
            try (PreparedStatement pstmtArticle = conn.prepareStatement(getArticleIDQuery)) {
                pstmtArticle.setString(1, title);
                ResultSet rsArticle = pstmtArticle.executeQuery();
                if (rsArticle.next()) {
                    articleID = rsArticle.getInt("articleID");
                } else {
                    System.out.println("Article not found: " + title);
                    return;
                }
            }

            // Insert into UserActions
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertUserActionQuery)) {
                pstmtInsert.setInt(1, userID);
                pstmtInsert.setInt(2, articleID);
                int rowsInserted = pstmtInsert.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Like action added successfully for user: " + userName);
                } else {
                    System.out.println("Failed to add like action.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.out.println("Error adding like action: " + e.getMessage());
        }
    }


    // For dislikeArticle method -- UserActions class
    public static void addDislikeUserAction(String userName, String title) {
        // Query to get userID based on userName
        String getUserIDQuery = "SELECT userID FROM User WHERE userName = ?";

        // Query to get articleID based on title
        String getArticleIDQuery = "SELECT articleID FROM Articles WHERE title = ?";

        // Query to insert into UserActions
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action) VALUES (?, ?, 'dislike')";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Enable transaction management

            // Retrieve userID
            int userID = -1;
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIDQuery)) {
                pstmtUser.setString(1, userName);
                ResultSet rsUser = pstmtUser.executeQuery();
                if (rsUser.next()) {
                    userID = rsUser.getInt("userID");
                } else {
                    System.out.println("User not found: " + userName);
                    return;
                }
            }

            // Retrieve articleID
            int articleID = -1;
            try (PreparedStatement pstmtArticle = conn.prepareStatement(getArticleIDQuery)) {
                pstmtArticle.setString(1, title);
                ResultSet rsArticle = pstmtArticle.executeQuery();
                if (rsArticle.next()) {
                    articleID = rsArticle.getInt("articleID");
                } else {
                    System.out.println("Article not found: " + title);
                    return;
                }
            }

            // Insert into UserActions
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertUserActionQuery)) {
                pstmtInsert.setInt(1, userID);
                pstmtInsert.setInt(2, articleID);
                int rowsInserted = pstmtInsert.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Dislike action added successfully for user: " + userName);
                } else {
                    System.out.println("Failed to add dislike action.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.out.println("Error adding dislike action: " + e.getMessage());
        }

    }


}