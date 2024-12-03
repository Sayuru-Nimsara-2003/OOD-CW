public class UserActions {
    private User user;

    public UserActions(User user){
        this.user = user;
    }

    public void viewArticle(User user, String articleTitle){
        // View the article in a window
        DatabaseHandler.addViewUserAction(user.getUserName(), articleTitle);
    }

    public void likeArticle(User user, String articleTitle){
        DatabaseHandler.addLikeUserAction(user.getUserName(), articleTitle);
    }

    public void dislikeArticle(User user, String articleTitle){
        DatabaseHandler.addDislikeUserAction(user.getUserName(), articleTitle);
    }

    public void saveArticle(){
        // get a relative path and copy the file in that path to another given path
    }
}
