import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Create a connection to the SQLite database
        Connection conn = DatabaseHandler.connect();

        // Perform database operations here

        DatabaseHandler.createTables();

        // Close the connection
        DatabaseHandler.closeConnection(conn);
    }


}
