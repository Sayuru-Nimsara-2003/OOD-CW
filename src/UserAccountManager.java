import java.util.Scanner;

public interface UserAccountManager {  // Could be an interface
    public abstract void register(Scanner scanner);
    public abstract void login(Scanner scanner);
    public abstract void deleteAccount(String userName, String password);
    public abstract void goPremium(String userName);

}

