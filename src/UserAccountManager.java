import java.util.Scanner;

public interface UserAccountManager {  // Could be an interface

    public abstract void login(User user, String userName, String password);
    public abstract void Register(User user, String userName, String password);

}

