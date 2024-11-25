import java.util.ArrayList;

public class UserAccountServices implements UserAccountManager{
    private ArrayList<User> users = new ArrayList<>();

    @Override
    public void register(String userName, String password){
        boolean userNameOk = false;
        boolean passwordOk = false;
        // Check in the DB if the username exists
        if (DatabaseHandler.doesUserNameExist(userName)){
            System.out.println("The userName " + userName + " already exists");
        } else {
            userNameOk = userNameValidation(userName);
            passwordOk = passwordValidation(password);
        }
        if (userNameOk && passwordOk){
            DatabaseHandler.addNewUser(userName, password);
        }
    }

    public boolean passwordValidation(String password){
        if (password.length() < 8) {
            System.out.println("Password must be at least 8 characters long");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            System.out.println("Password must have at least one capital letter");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            System.out.println("Password must have at least one lowercase letter");
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            System.out.println("Password must have at least one number");
            return false;
        }
        return true;
    }

    public boolean userNameValidation(String userName){
        if (userName.length() < 3 && userName.length() > 15){
            System.out.println("Username should be length between 3 and 15 characters");
            return false;
        }
        if (!Character.isAlphabetic(userName.charAt(0))){
            System.out.println("First letter should be an alphabetic character");
            return false;
        }
        return true;
    }

    @Override
    public void login(String userName, String password){
        if (!DatabaseHandler.doesUserNameExist(userName)){
            System.out.println("User name doesn't exists");
        } else {
            if (!DatabaseHandler.passwordMatching(userName, password)){
                System.out.println("Password doesn't match with the entered userName");
            }
        }
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
