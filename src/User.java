import java.util.ArrayList;

public class User {
    private String userID;
    private String userName;
    private String password;
    private boolean isPremium;
    private ArrayList<Article> viewedArticles;

    public User(){
        this.isPremium = false;
    }


}
