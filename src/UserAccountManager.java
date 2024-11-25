public interface UserAccountManager {  // Could be an interface
    public abstract void register(String userName, String password);
    public abstract void login(String userName, String password);
    public abstract void deleteAccount(String userName, String password);
    public abstract void goPremium(String userName);

}

