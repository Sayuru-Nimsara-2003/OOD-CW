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

    // Method to create a database connection
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
        int userId = assignUserID();
        user.setUserId(userId);

        String insertUser = "INSERT INTO User (userID, userName, password, isAdmin) VALUES (?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertUser)) {

            pstmt.setInt(1, userId);        // Auto-assigned userID
            pstmt.setString(2, userName);   // Provided userName
            pstmt.setString(3, password);   // Provided password
            pstmt.setInt(4, 0);             // isAdmin is always 0 (no admin rights)

            pstmt.executeUpdate();
            System.out.println(GREEN + "New user added successfully with userID: " + userId + "\n" + RESET);

        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    // Method to get userID by userName
    public static int getUserIdByUserName(String userName) {
        int userId = -1;  // Default value to return if user is not found

        // SQL query to get the userID from the User table based on the userName
        String query = "SELECT userID FROM User WHERE userName = ?";

        // Establish the connection and execute the query
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the userName parameter in the query
            pstmt.setString(1, userName);

            // Execute the query and retrieve the result
            ResultSet rs = pstmt.executeQuery();

            // If a result is found, retrieve the userID
            if (rs.next()) {
                userId = rs.getInt("userID");  // Get userID from result set
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving user ID: " + e.getMessage());
        }

        return userId;  // Return the userID (or -1 if not found)
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

    // Method to check if an article exists by title
    public static boolean doesArticleExist(String title) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // Establish a database connection
            connection = connect();

            // SQL query to check if an article with the given title exists
            String query = "SELECT articleID FROM Articles WHERE title = ?";

            // Prepare the statement
            statement = connection.prepareStatement(query);
            statement.setString(1, title);

            // Execute the query
            resultSet = statement.executeQuery();

            // If resultSet has data, the article exists
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // In case of an error, return false
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
        String insertArticleQuery = "INSERT INTO Articles (articleID, title, content, category, articleLink) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertArticleQuery)) {

            // Set the values for the placeholders
            pstmt.setInt(1, articleID); // Set articleID
            pstmt.setString(2, title);  // Set title
            pstmt.setString(3, content); // Set content
            pstmt.setString(4, category); // Set category
            pstmt.setString(5, path); // Set articleLink

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
    public static void addViewUserAction(int userID, int articleID) {
        // Query to insert into UserActions
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
                if (rowsInserted > 0) {
                    System.out.println("View action added successfully for user: " + userID);
                } else {
                    System.out.println("Failed to add view action.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.out.println("Error adding view action: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // For likeArticle method -- UserActions class
    public static void addLikeUserAction(int userID, int articleID) {
        // Query to insert into UserActions
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
                    System.out.println("Like action added successfully for user ID: " + userID);
                } else {
                    System.out.println("Failed to add like action.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.out.println("Error adding like action: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // For dislikeArticle method -- UserActions class
    public static void addDislikeUserAction(int userID, int articleID) {
        // Query to insert into UserActions
        String insertUserActionQuery = "INSERT INTO UserActions (userID, articleID, action, timestamp) VALUES (?, ?, 'dislike', ?)";

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
                    System.out.println("Dislike action added successfully for user ID: " + userID);
                } else {
                    System.out.println("Failed to add dislike action.");
                }
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.out.println("Error adding dislike action: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // Create a array list of all existing articles (Only when the program starts)
    public static void retrieveAllArticlesFromDB(){

        // SQL query to select all articles from the table
        String selectArticlesQuery = "SELECT * FROM Articles;";


        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Execute the query to retrieve articles
            ResultSet rs = stmt.executeQuery(selectArticlesQuery);

            // Temporary list to store retrieved articles
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

        // SQL query to insert the new article
        String insertArticleSQL = "INSERT INTO Articles (articleID, title, content, category, articleLink) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertArticleSQL)) {

            // Set the parameters for the insert statement
            pstmt.setInt(1, articleID);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, category);
            pstmt.setString(5, link);

            // Execute the insert
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
        // Database connection details
        String url = "jdbc:sqlite:news_recommendation_system.db";
        String query = "SELECT userID, articleID, action FROM UserActions";

        // Prepare the CSV writer
        try (FileWriter writer = new FileWriter(filePath);
             Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Process each record from the ResultSet
            while (rs.next()) {
                int userID = rs.getInt("userID");
                int articleID = rs.getInt("articleID");
                String action = rs.getString("action");

                // Determine the value based on the action
                int actionValue = 0;
                switch (action) {
                    case "view":
                        actionValue = 1;
                        break;
                    case "dislike":
                        actionValue = -1;
                        break;
                    case "like":
                        actionValue = 2;
                        break;
                    default:
                        System.err.println("Unknown action type: " + action);
                        continue; // Skip this row if the action is unknown
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

    public static void main(String[] args) {
        generateUserActionsCSV("Data/ActionScores.csv");
    }


}