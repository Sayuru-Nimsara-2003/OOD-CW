public class UserActions {
    private User user;

    public UserActions(User user){
        this.user = user;
    }

    public void viewArticle(int userID, int articleID){
        DatabaseHandler.addViewUserAction(userID, articleID);
    }

    public void likeArticle(int userID, int articleID){
        DatabaseHandler.addLikeUserAction(userID, articleID);
    }

    public void dislikeArticle(int userID, int articleID){
        DatabaseHandler.addDislikeUserAction(userID, articleID);
    }

}
