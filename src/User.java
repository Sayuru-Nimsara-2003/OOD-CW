import java.util.ArrayList;

public class User {
    private String userID;
    private String userName;
    private String password;
    private boolean isPremium;
    private ArrayList<Article> viewedArticles;

    public User() {
        this.isPremium = false;
    }

}


class PremiumUser extends User{

    // Implement save methods
    public void saveArticle(){
        // Show only uf the account is premium
        // copy the file from the server to the given path as a pdf

    }
}
