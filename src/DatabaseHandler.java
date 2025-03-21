import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DatabaseHandler {
    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text
    private static final String URL = "jdbc:sqlite:news_recommendation_system.db";

    // Create the database connection
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Error occurred while connecting to the database.");
            e.printStackTrace();
        }
        return conn;
    }

    // Close the connection
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
                "isAdmin INTEGER NOT NULL DEFAULT 0" +  // Make sure isAdmin is included
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
                "articleLink TEXT NOT NULL" +
                ");";


        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createUserActionsTable);
            stmt.execute(createArticlesTable);
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
    public static void addNewUser(User user, String userName, String password) {
        // Auto assign the id based on the user table
        int userId = assignUserID();
        user.setUserId(userId);

        String insertUser = "INSERT INTO User (userID, userName, password, isAdmin) VALUES (?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertUser)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, userName);
            pstmt.setString(3, password);
            pstmt.setInt(4, 0);

            pstmt.executeUpdate();


        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    // Method to get userID by userName
    public static int getUserIdByUserName(String userName) {
        int userId = -1;  // Default value to return if user is not found

        String query = "SELECT userID FROM User WHERE userName = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userName);

            ResultSet rs = pstmt.executeQuery();

            // If a result is found, retrieve the userID
            if (rs.next()) {
                userId = rs.getInt("userID");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user ID: " + e.getMessage());
        }

        return userId;  // Return the userID (or -1 if not found)
    }

    // For login method --- userAccountServices class
    public static boolean passwordMatching(String userName, String password) {
        String query = "SELECT password FROM User WHERE userName = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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

    // For Driver class to check if a admin is logging

    public static boolean isAdminUser(String userName) {
        String query = "SELECT isAdmin FROM User WHERE userName = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("isAdmin") == 1;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking admin status: " + e.getMessage());
        }

        return false;
    }

    // Method to check if an article exists by title
    public static boolean doesArticleExist(String title) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = connect();

            String query = "SELECT articleID FROM Articles WHERE title = ?";

            statement = connection.prepareStatement(query);
            statement.setString(1, title);

            resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    public static int assignArticleID(){
        String query = "SELECT MAX(ArticleID) FROM Articles;";
        int newArticleID = 1; // Default starting userID

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int lastArticleID = rs.getInt(1); // Get the MAX(userID)
                if (!rs.wasNull()) {
                    newArticleID = lastArticleID + 1;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error assigning ArticleOD: " + e.getMessage());
        }

        return newArticleID;
    }


    // For viewArticles method in UserActions class
    public static void addViewUserAction(int userID, int articleID) {
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action, timestamp) VALUES (?, ?, 'view', ?)";

        // Generate the current timestamp in the format 'yyyy-MM-dd HH:mm:ss'
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Enable transaction management

            // Insert into UserActions
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertUserActionQuery)) {
                pstmtInsert.setInt(1, userID);
                pstmtInsert.setInt(2, articleID);
                pstmtInsert.setString(3, timestamp);

                int rowsInserted = pstmtInsert.executeUpdate();

            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Error adding view action: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // For likeArticle method -- UserActions class
    public static void addLikeUserAction(int userID, int articleID) {
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action, timestamp) VALUES (?, ?, 'like', ?)";

        // Generate the current timestamp in the format 'yyyy-MM-dd HH:mm:ss'
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Enable transaction management

            // Insert into UserActions
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertUserActionQuery)) {
                pstmtInsert.setInt(1, userID);
                pstmtInsert.setInt(2, articleID);
                pstmtInsert.setString(3, timestamp);

                int rowsInserted = pstmtInsert.executeUpdate();
                if (rowsInserted > 0) {
//                    System.out.println("Dislike action added successfully for user ID: " + userID);
                } else {
                    System.out.println("Failed to add like action.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Error adding like action: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // For dislikeArticle method -- UserActions class
    public static void addDislikeUserAction(int userID, int articleID) {
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action, timestamp) VALUES (?, ?, 'dislike', ?)";

        // Generate the current timestamp in the format 'yyyy-MM-dd HH:mm:ss'
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // Insert into UserActions
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertUserActionQuery)) {
                pstmtInsert.setInt(1, userID);
                pstmtInsert.setInt(2, articleID);
                pstmtInsert.setString(3, timestamp);

                int rowsInserted = pstmtInsert.executeUpdate();
                if (rowsInserted > 0) {
//                    System.out.println("Dislike action added successfully for user ID: " + userID);
                } else {
                    System.out.println("Failed to add dislike action.");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Error adding dislike action: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // Create a array list of all existing articles (Only when the program starts)
    public static void retrieveAllArticlesFromDB(){

        String selectArticlesQuery = "SELECT * FROM Articles;";


        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(selectArticlesQuery);

            ArrayList<Article> articles = new ArrayList<>();

            // Iterate through the result set
            while (rs.next()) {
                // Retrieve data for each article
                int articleID = rs.getInt("articleID");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String category = rs.getString("category");
                String articleLink = rs.getString("articleLink");

                // Create an Article instance
                Article article = new Article(articleID, title, content, category, articleLink);

                // Add the Article to the list
                articles.add(article);
            }

            // Set the retrieved articles in the ArticleManager
            ArticleManager.allArticles = articles;


        } catch (SQLException e) {
            System.out.println("Error accessing the database: " + e.getMessage());
        }

    }


    // Add an article -- ArticleManager class
    public static void addNewArticle(String title, String content, String category, String link) {
        int articleID = assignArticleID();
        Article newArticle = new Article(articleID, title, content, category, link);
        ArticleManager.allArticles.add(newArticle);

        String insertArticleSQL = "INSERT INTO Articles (articleID, title, content, category, articleLink) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertArticleSQL)) {

            pstmt.setInt(1, articleID);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, category);
            pstmt.setString(5, link);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Article added successfully with ID: " + articleID);
            } else {
                System.out.println("Failed to add article.");
            }
        } catch (SQLException e) {
            System.out.println("Error while adding article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // BELOW METHODS ARE ONLY USED FOR ADDING DATA INITIALLY TO THE DATABASE

    // Populate Users table
    public static void addUsersFromCSV(String filePath) {
        String insertUserSQL = "INSERT INTO User (userID, userName, password, isAdmin) VALUES (?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertUserSQL);
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // Skip the header line
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(","); // Split CSV line by commas
                int userID = Integer.parseInt(values[0]);
                String userName = values[1];
                String password = values[2];
                int isAdmin = Integer.parseInt(values[3]);

                // Set values in the PreparedStatement
                pstmt.setInt(1, userID);
                pstmt.setString(2, userName);
                pstmt.setString(3, password);
                pstmt.setInt(4, isAdmin);

                pstmt.addBatch(); // Add to batch for efficiency
            }

            pstmt.executeBatch(); // Execute all batched queries
            System.out.println("Users added successfully from CSV.");

        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error inserting users into database: " + e.getMessage());
        }
    }

    // Method to check the schema of the User table
    public static void checkTableSchema() {
        String query = "PRAGMA table_info(User);";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:news_recommendation_system.db"); // Use your actual database path
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Print the column names and types
            System.out.println("Column info for 'User' table:");
            while (rs.next()) {
                int columnID = rs.getInt("cid");
                String columnName = rs.getString("name");
                String columnType = rs.getString("type");
                boolean isNotNull = rs.getInt("notnull") == 1;
                String defaultValue = rs.getString("dflt_value");
                boolean isPrimaryKey = rs.getInt("pk") == 1;

                System.out.println("Column ID: " + columnID);
                System.out.println("Column Name: " + columnName);
                System.out.println("Column Type: " + columnType);
                System.out.println("Is Not Null: " + isNotNull);
                System.out.println("Default Value: " + defaultValue);
                System.out.println("Is Primary Key: " + isPrimaryKey);
                System.out.println("-------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error checking table schema: " + e.getMessage());
        }
    }

    // Method to drop all tables in the database
    public static void dropAllTables() {
        String query = "SELECT name FROM sqlite_master WHERE type='table';";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Loop through all tables and drop them
            while (rs.next()) {
                String tableName = rs.getString("name");

                // Skip the sqlite_master table itself
                if ("sqlite_master".equals(tableName)) {
                    continue;
                }

                String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
                try (Statement dropStmt = conn.createStatement()) {
                    dropStmt.executeUpdate(dropTableSQL);
                    System.out.println("Dropped table: " + tableName);
                } catch (SQLException e) {
                    System.out.println("Error dropping table " + tableName + ": " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching table names: " + e.getMessage());
        }
    }

    // Method to populate the UserActions table from a CSV file
    public static void addUserActionsFromCSV(String filePath) {
        String insertUserActionSQL = "INSERT INTO UserActions (userID, articleID, action, timestamp) VALUES (?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(URL); // Replace with your actual URL
             PreparedStatement pstmt = conn.prepareStatement(insertUserActionSQL);
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // Skip the header line
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(","); // Split CSV line by commas
                int userID = Integer.parseInt(values[0]);
                int articleID = Integer.parseInt(values[1]);
                String action = values[2];
                String timestamp = values[3]; // Make sure timestamp is in the correct format

                // Set values in the PreparedStatement
                pstmt.setInt(1, userID);
                pstmt.setInt(2, articleID);
                pstmt.setString(3, action);
                pstmt.setString(4, timestamp);

                pstmt.addBatch(); // Add to batch for efficiency
            }

            pstmt.executeBatch(); // Execute all batched queries
            System.out.println("User actions added successfully from CSV.");

        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error inserting user actions into database: " + e.getMessage());
        }
    }

    public static void generateUserActionsCSV(String filePath) {
        String url = "jdbc:sqlite:news_recommendation_system.db";
        String query = "SELECT userID, articleID, action FROM UserActions";

        try (FileWriter writer = new FileWriter(filePath);
             Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Process each rows from the ResultSet
            while (rs.next()) {
                int userID = rs.getInt("userID");
                int articleID = rs.getInt("articleID");
                String action = rs.getString("action");

                // Assign values for each action
                int actionValue = 0;
                switch (action) {
                    case "view":
                        actionValue = 1;
                        break;
                    case "dislike":
                        actionValue = -2;
                        break;
                    case "like":
                        actionValue = 2;
                        break;
                    default:
                        System.err.println("Unknown action type: " + action);
                        continue;
                }

                // Write the row in CSV format: userID, articleID, actionValue
                writer.append(String.format("%d,%d,%d%n", userID, articleID, actionValue));
            }

//            System.out.println("CSV file has been generated successfully!");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to check if an action already exists in the UserActions table
    public static boolean checkActionExists(int userID, int articleID, String action) {
        String query = "SELECT 1 FROM UserActions WHERE userID = ? AND articleID = ? AND action = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, articleID);
            pstmt.setString(3, action);

            try (ResultSet rs = pstmt.executeQuery()) {
                // If the query returns a result, the action exists
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking action: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Method to delete a specific row from UserActions table
    public static void deleteUserAction(int userID, int articleID, String action) {
        String query = "DELETE FROM UserActions WHERE userID = ? AND articleID = ? AND action = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, articleID);
            pstmt.setString(3, action);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No matching action found to delete.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting action: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        generateUserActionsCSV("Data/ActionScores.csv");
    }


}