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

        // Create ActionScores.csv
        DatabaseHandler.generateUserActionsCSV("Data/ActionScores.csv");


        // Initial start of the UI
        initialMenu(user, scanner);
    }

    public static void initialMenu(User user, Scanner scanner){
        System.out.println(BLUE + "\nHello, Welcome to articleReader" ); // Change text color and size if possible
        System.out.println("---------------------------------------" + RESET);

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
                System.out.println(RED + "Invalid Input! Retry\n"+ RESET);
            }
        }
    }

    public static void initialRegisterMenu(User user, Scanner scanner) {
        boolean userNameOK = false;
        boolean passwordOK = false;
        String userNameInput = "";
        String passwordInput = "";

        while (true) {
            System.out.println(BLUE + "\nRegister Menu:" + RESET);
            System.out.println("  To enter username, enter U");
            System.out.println("  To enter password, enter P");
            System.out.println("  To go back to the previous menu, press B");
            System.out.print(BLUE + "\tEnter your input: " + RESET);
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("u")) {
                System.out.print("  Enter username: ");
                userNameInput = scanner.nextLine().trim();

                // Check if the username exists and is valid
                if (DatabaseHandler.doesUserNameExist(userNameInput)) {
                    System.out.println(RED + "User already exists" + RESET);
                    userNameOK = false;
                } else if (UserAccountServices.userNameValidation(userNameInput)) {
                    userNameOK = true;
                    System.out.println(GREEN + "  Username accepted." + RESET);
                } else {
                    System.out.println(RED + "  Invalid username. Please try again." + RESET);
                }

            } else if (userInput.equalsIgnoreCase("p")) {
                if (userNameOK) {
                    System.out.print("  Enter password: ");
                    passwordInput = scanner.nextLine().trim();

                    if (UserAccountServices.PasswordValidation(passwordInput)) {
                        passwordOK = true;
                        System.out.println(GREEN + "  Password accepted." + RESET);
                    } else {
                        System.out.println(RED + "  Invalid password. Please try again." + RESET);
                    }
                } else {
                    System.out.println(RED + "  Please provide a valid username first in Register Menu." + RESET);
                }

            } else if (userInput.equalsIgnoreCase("b")) {
                // Return to the initial menu
                initialMenu(user, scanner);
                return;

            } else {
                System.out.println(RED + "  Invalid input. Please try again." + RESET);
            }

            // If both validations pass, register the user
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
        String userInput = "";
        String userNameInput = "";
        String passwordInput = "";

        while (true) {
            System.out.println(BLUE + "\nLogin Menu:" + RESET);
            System.out.println("  To enter username, enter U");
            System.out.println("  To enter password, enter P");
            System.out.println("  To go back to the previous menu, press B");
            System.out.print(BLUE + "\tEnter your input: " + RESET);
            userInput = scanner.nextLine();


            if (userInput.equalsIgnoreCase("u")) {
                System.out.print("  Enter username: ");
                userNameInput = scanner.nextLine();
                userNameExists = DatabaseHandler.doesUserNameExist(userNameInput);
                if (!userNameExists){
                    System.out.println(RED + "  Username doesn't exist" + RESET);
                }

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
            } else {
                System.out.println(RED + "  Invalid input. Please try again." + RESET);
            }

            if (userNameExists && passwordInput != ""){
                if (userNameExists && !passwordMatches){
                    System.out.println(RED + "Password doesn't match with username" + RESET);
                }
            }

            if (userNameExists && passwordMatches) {
                UserAccountServices loginService = new UserAccountServices();
                loginService.login(user,userNameInput, passwordInput);
                System.out.println(GREEN + "Successfully logged in" + RESET);
                if (DatabaseHandler.isAdminUser(userNameInput)){
                    adminMainMenu(user, scanner);
                } else {
                    normalMainMenu(user, scanner);
                }
            }
        }

    }

    // Main menu for admins
    public static void adminMainMenu(User user, Scanner scanner){
        String userInput;

        while (true){
            System.out.println(BLUE + "\nAdmin Main Menu" + RESET);
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
            System.out.println(BLUE + "\nMain Menu" + RESET);
            System.out.println(" To access all articles enter A");
            System.out.println(" To get recommendations enter R");
            System.out.println(" To logout enter L");
            System.out.println(" To quit application press Q ");
            System.out.print(BLUE + "\tEnter your input: " + RESET);
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("a")){
                // Directed to ArticleManager class for further UI parts
                ArticleManager.displayArticlesMenu(user, scanner);

            } else if (userInput.equalsIgnoreCase("r")) {
                //Directed to recommendations menu
                RecommendationEngine.recommendationMenu(user,scanner);

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
