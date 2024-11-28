import java.sql.SQLOutput;
import java.util.Scanner;

public class User implements UserAccountManager{

    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text

    @Override
    public void register(Scanner scanner){

    }

    public static boolean PasswordValidation(String password){
        if (password.length() < 8) {
            System.out.println(RED + "Password must be at least 8 characters long" + RESET);
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            System.out.println(RED + "Password must have at least one capital letter" + RESET);
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            System.out.println(RED + "Password must have at least one lowercase letter" + RESET);
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            System.out.println(RED + "Password must have at least one number" + RESET);
            return false;
        }
        System.out.println(GREEN + "Password is entered successfully" + RESET);
        return true;
    }

    public static boolean userNameValidation(String userName){
        if (userName.length() < 3 || userName.length() > 15){
            System.out.println(RED + "Username should be length between 3 and 15 characters" + RESET);
            return false;
        }
        if (!Character.isAlphabetic(userName.charAt(0))){
            System.out.println(RED + "First letter should be an alphabetic character" + RESET);
            return false;
        }
        System.out.println(GREEN + "User Name entered successfully" + RESET);
        return true;
    }


    @Override
    public void login(Scanner scanner) {

    }

    @Override
    public void deleteAccount(String userName, String password){
        if (!DatabaseHandler.doesUserNameExist(userName)){
            System.out.println("User name doesn't exists");
        } else {
            if (!DatabaseHandler.passwordMatching(userName, password)){
                System.out.println("Password doesn't match with the entered userName");
            } else {
                DatabaseHandler.removeUser(userName);
            }
        }
    }

    @Override
    public void goPremium(String userName){
        DatabaseHandler.goPremium(userName);
    }
}
