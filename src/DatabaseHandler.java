import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
                "articleLink TEXT NOT NULL," +
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

            System.out.println("Articles retrieved successfully and stored in ArticleManager.");

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

    public static void main(String[] args) {
        String filePath = "Data/UserTable data.csv"; // Path to your CSV file
        addUsersFromCSV(filePath);
    }


}