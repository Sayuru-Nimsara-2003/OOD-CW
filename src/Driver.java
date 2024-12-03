import java.sql.SQLOutput;
import java.util.*;

public class Driver {

    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text

    public static void main(String[] args) {
        // Scanner instance
        Scanner scanner = new Scanner(System.in);

        // Status
        boolean loggedIn = false;

        // Login or Register select
        DatabaseHandler.connect();
        initialLoginMenu(scanner);
    }

    public static void initialMenu(Scanner scanner){
        System.out.println("Hello, Welcome to articleViewer"); // Change text color and size if possible
        System.out.println("---------------------------------------");

        boolean validInput = false;

        while (validInput == false) {
            System.out.println("Enter R to register");
            System.out.println("Enter L to login");
            System.out.println("Enter Q to quit");
            System.out.print("\tEnter your input : ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("r")) {
                validInput = true;
                initialRegisterMenu(scanner);
            } else if (userInput.equalsIgnoreCase("l")) {
                validInput = true;
                // go to the initial_register menu
            } else if (userInput.equalsIgnoreCase("q")) {
                validInput = true;
                // quit the program
            } else {
                System.out.println(RED + "Invalid Input! Retry\n"+ RESET); // Red color
            }
        }
    }

    public static void initialRegisterMenu(Scanner scanner) {
        boolean userNameOK = false;
        boolean passwordOK = false;
        String userInput;

        while (true) {
            System.out.println(BLUE + "\nRegister Menu:" + RESET);
            System.out.println("  To enter username, enter U");
            System.out.println("  To enter password, enter P");
            System.out.println("  To go back to the previous menu, press B");
            System.out.print("\tEnter your input: ");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("u")) {
                System.out.print("  Enter username: ");
                String userNameInput = scanner.nextLine();
                userNameOK = UserServices.userNameValidation(userNameInput);
            } else if (userInput.equalsIgnoreCase("p")) {
                if (userNameOK) {
                    System.out.print("  Enter password: ");
                    String passwordInput = scanner.nextLine();
                    passwordOK = UserServices.PasswordValidation(passwordInput);
                } else {
                    System.out.println(RED + "  Please provide a valid username first in Register Menu" + RESET);
                }
            } else if (userInput.equalsIgnoreCase("b")) {
                initialMenu(scanner);
                return;
            } else {
                System.out.println(RED + "  Invalid input. Please try again." + RESET);
            }

            if (userNameOK && passwordOK) {
                System.out.println(GREEN + "\nSuccessfully registered!" + RESET);
                // Use the method to make an instance of User
                // Go to mainMenu()
                return;
            }
        }
    }

    public static void initialLoginMenu(Scanner scanner){
        boolean userNameExists = false;
        boolean passwordMatches = false;
        String userInput;

        while (true) {
            System.out.println(BLUE + "\nLogin Menu:" + RESET);
            System.out.println("  To enter username, enter U");
            System.out.println("  To enter password, enter P");
            System.out.println("  To go back to the previous menu, press B");
            System.out.print("\tEnter your input: ");
            userInput = scanner.nextLine();

            String userNameInput = "";
            if (userInput.equalsIgnoreCase("u")) {
                System.out.print("  Enter username: ");
                userNameInput = scanner.nextLine();
                userNameExists = DatabaseHandler.doesUserNameExist(userNameInput);

            } else if (userInput.equalsIgnoreCase("p")) {
                if (userNameExists) {
                    System.out.print("  Enter password: ");
                    String passwordInput = scanner.nextLine();
                    passwordMatches = DatabaseHandler.passwordMatching(userNameInput, passwordInput);
                } else {
                    System.out.println(RED + "  Please provide the username first in Login Menu" + RESET);
                }
            } else if (userInput.equalsIgnoreCase("b")) {
                initialMenu(scanner);
                return;
            } else {
                System.out.println(RED + "  Invalid input. Please try again." + RESET);
            }

            if (userNameExists && passwordMatches) {
                System.out.println(GREEN + "\nSuccessfully logged in!" + RESET);
                // Use the method to make an instance of User
                if (DatabaseHandler.isAdminUser(userNameInput)){
                    adminMainMenu(scanner);
                } else {
                    normalMainMenu(scanner);
                }
                return;
            }
        }

    }

    // Main menu for admins
    public static void adminMainMenu(Scanner scanner){
        String userInput;

        while (true){
            System.out.println(BLUE + "Admin Main Menu" + RESET);
            System.out.println(" To add articles enter A");
            System.out.println(" To remove articles enter R");
            System.out.println(" To logout enter L");
            System.out.println(" To quit application press Q ");
            System.out.print("\tEnter your input: ");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("a")){
                ArticleManager.displayAllArticle();
                // Do the other UI part
                return;
            } else if (userInput.equalsIgnoreCase("r")) {
                // Method to remove articles from the database and remove the article file
                return;
            } else if (userInput.equalsIgnoreCase("l")) {
                // Remove userReferences from the instance
                initialMenu(scanner);
                return;
            } else if (userInput.equalsIgnoreCase("q")) {
                //quit
            } else {
                System.out.println(RED + "Invalid userInput try again!" + RESET);
            }

        }
    }

    // Main menu for normal users
    public static void normalMainMenu(Scanner scanner){
        String userInput;

        while (true) {
            System.out.println(BLUE + "Main Menu" + RESET);
            System.out.println(" To read all articles enter A");
            System.out.println(" To get recommendations enter R");
            System.out.println(" To logout enter L");
            System.out.println(" To quit application press Q ");
            System.out.println("\tEnter your input: ");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("a")){
                ArticleManager.displayAllArticle();
                // Do the remaining UI part
                return;
            } else if (userInput.equalsIgnoreCase("r")) {
                // Go to view recommended articles menu
                return;
            } else if (userInput.equalsIgnoreCase("l")) {
                // Remove userReferences from the instance
                initialMenu(scanner);
                return;
            } else if (userInput.equalsIgnoreCase("q")) {
                //quit
            } else {
                System.out.println(RED + "Invalid userInput try again!" + RESET);
            }
        }
    }



}
