import java.sql.SQLOutput;
import java.util.*;

public class Driver {

    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text

    public static void main(String[] args) {
        // Connect to the database
        DatabaseHandler.connect();

        // Scanner instance
        Scanner scanner = new Scanner(System.in);

        //Initial user instance
        User user = new User();

        // Create instances for all existing articles
        DatabaseHandler.retrieveAllArticlesFromDB();

        // Initial start of the UI
        initialMenu(user, scanner);
    }

    public static void initialMenu(User user, Scanner scanner){
        System.out.println("Hello, Welcome to articleViewer"); // Change text color and size if possible
        System.out.println("---------------------------------------");

        boolean validInput = false;

        while (validInput == false) {
            System.out.println("Enter R to register");
            System.out.println("Enter L to login");
            System.out.println("Enter Q to quit");
            System.out.print(BLUE + "\tEnter your input : " + RESET);
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("r")) {
                validInput = true;
                initialRegisterMenu(user, scanner);
            } else if (userInput.equalsIgnoreCase("l")) {
                validInput = true;
                initialLoginMenu(user, scanner);
            } else if (userInput.equalsIgnoreCase("q")) {
                validInput = true;
                System.exit(0);
            } else {
                System.out.println(RED + "Invalid Input! Retry\n"+ RESET); // Red color
            }
        }
    }

    public static void initialRegisterMenu(User user, Scanner scanner) {
        boolean userNameOK = false;
        boolean passwordOK = false;
        String userInput;

        while (true) {
            System.out.println(BLUE + "\nRegister Menu:" + RESET);
            System.out.println("  To enter username, enter U");
            System.out.println("  To enter password, enter P");
            System.out.println("  To go back to the previous menu, press B");
            System.out.print(BLUE + "\tEnter your input: " + RESET);
            userInput = scanner.nextLine();

            String userNameInput = "";
            String passwordInput = "";

            if (userInput.equalsIgnoreCase("u")) {
                System.out.print("  Enter username: ");
                userNameInput = scanner.nextLine();
                userNameOK = UserAccountServices.userNameValidation(userNameInput);
            } else if (userInput.equalsIgnoreCase("p")) {
                if (userNameOK) {
                    System.out.print("  Enter password: ");
                    passwordInput = scanner.nextLine();
                    passwordOK = UserAccountServices.PasswordValidation(passwordInput);
                } else {
                    System.out.println(RED + "  Please provide a valid username first in Register Menu" + RESET);
                }
            } else if (userInput.equalsIgnoreCase("b")) {
                initialMenu(user, scanner);
                return;
            } else {
                System.out.println(RED + "  Invalid input. Please try again." + RESET);
            }

            if (userNameOK && passwordOK) {
                System.out.println(GREEN + "\nSuccessfully registered!" + RESET);

                UserAccountServices register = new UserAccountServices();
                register.Register(user, userNameInput, passwordInput);

                // Go to main menu
                normalMainMenu(user, scanner);
                return;
            }
        }
    }

    public static void initialLoginMenu(User user, Scanner scanner){
        boolean userNameExists = false;
        boolean passwordMatches = false;
        String userInput;

        while (true) {
            System.out.println(BLUE + "\nLogin Menu:" + RESET);
            System.out.println("  To enter username, enter U");
            System.out.println("  To enter password, enter P");
            System.out.println("  To go back to the previous menu, press B");
            System.out.print(BLUE + "\tEnter your input: " + RESET);
            userInput = scanner.nextLine();

            String userNameInput = "";
            String passwordInput = "";
            if (userInput.equalsIgnoreCase("u")) {
                System.out.print("  Enter username: ");
                userNameInput = scanner.nextLine();
                userNameExists = DatabaseHandler.doesUserNameExist(userNameInput);

            } else if (userInput.equalsIgnoreCase("p")) {
                if (userNameExists) {
                    System.out.print("  Enter password: ");
                    passwordInput = scanner.nextLine();
                    passwordMatches = DatabaseHandler.passwordMatching(userNameInput, passwordInput);
                } else {
                    System.out.println(RED + "  Please provide the username first in Login Menu" + RESET);
                }
            } else if (userInput.equalsIgnoreCase("b")) {
                initialMenu(user, scanner);
                return;
            } else {
                System.out.println(RED + "  Invalid input. Please try again." + RESET);
            }

            if (userNameExists && passwordMatches) {
                System.out.println(GREEN + "\nSuccessfully logged in!" + RESET);
                // Use the method to make an instance of User
                UserAccountServices loginService = new UserAccountServices();
                loginService.login(user,userNameInput, passwordInput);
                if (DatabaseHandler.isAdminUser(userNameInput)){
                    adminMainMenu(user, scanner);
                } else {
                    normalMainMenu(user, scanner);
                }
                return;
            }
        }

    }

    // Main menu for admins
    public static void adminMainMenu(User user, Scanner scanner){
        String userInput;

        while (true){
            System.out.println(BLUE + "Admin Main Menu" + RESET);
            System.out.println(" To add articles enter A");
            System.out.println(" To logout enter L");
            System.out.println(" To quit application press Q ");
            System.out.print(BLUE + "\tEnter your input: " + RESET);
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("a")){
                //Add articles menu
                AdminUser.addArticlesMenu(user,scanner);
            } else if (userInput.equalsIgnoreCase("l")) {
                UserAccountServices logoutService = new UserAccountServices();
                logoutService.logout(user);

                initialMenu(user, scanner);
            } else if (userInput.equalsIgnoreCase("q")) {
                System.exit(0);
            } else {
                System.out.println(RED + "Invalid userInput try again!" + RESET);
            }

        }
    }

    // Main menu for normal users
    public static void normalMainMenu(User user, Scanner scanner){
        String userInput;

        while (true) {
            System.out.println(BLUE + "Main Menu" + RESET);
            System.out.println(" To access all articles enter A");
            System.out.println(" To get recommendations enter R");
            System.out.println(" To logout enter L");
            System.out.println(" To quit application press Q ");
            System.out.println(BLUE + "\tEnter your input: " + BLUE);
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("a")){
                // Directed to ArticleManager class for further UI parts
                ArticleManager.displayArticlesMenu(user, scanner);

            } else if (userInput.equalsIgnoreCase("r")) {
                //

            } else if (userInput.equalsIgnoreCase("l")) {
                // Setting attributes to null for the user instance
                UserAccountServices logoutService = new UserAccountServices();
                logoutService.logout(user);

                initialMenu(user, scanner);

            } else if (userInput.equalsIgnoreCase("q")) {
                System.exit(0);
            } else {
                System.out.println(RED + "Invalid userInput try again!" + RESET);
            }
        }
    }



}
