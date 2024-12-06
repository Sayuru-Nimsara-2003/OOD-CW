public class UserAccountServices implements UserAccountManager{

    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text



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
        return true;
    }


    @Override
    public void login(User user, String userName, String password) {
        user.setUserName(userName);
        user.setPassword(password);
        user.setUserId(DatabaseHandler.getUserIdByUserName(userName));
    }

    @Override
    public void Register(User user, String userName, String password){
        user.setUserName(userName);
        user.setPassword(password);
        DatabaseHandler.addNewUser(user, userName, password);
    }

    @Override
    public void logout(User user){
        user.setUserId(-1);
        user.setUserName(null);
        user.setPassword(null);
    }


}
