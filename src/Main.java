import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
//        // Create a connection to the SQLite database
//        Connection conn = DatabaseHandler.connect();
//
//        // Perform database operations here
//
//        DatabaseHandler.createTables();
//
//        // Close the connection
//        DatabaseHandler.closeConnection(conn);
        try {
            // Provide the path to your PDF file
            File pdfFile = new File("Articles/OOD Revision Engagement Week.pdf");

            // Check if Desktop is supported
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (pdfFile.exists()) {
                    desktop.open(pdfFile); // Open the PDF file in the default viewer
                } else {
                    System.out.println("File does not exist!");
                }
            } else {
                System.out.println("Desktop is not supported on this system.");
            }
        } catch (IOException e) {
            System.out.println("Error opening the file: " + e.getMessage());
        }

    }


}
