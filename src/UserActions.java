public class UserActions {
    private User user;

    public void viewArticle(String userName, String articleTitle){
        // View the article in a window

        DatabaseHandler.addViewUserAction(userName, articleTitle);
    }

    public void likeArticle(String userName, String articleTitle){
        DatabaseHandler.addLikeUserAction(userName, articleTitle);
    }

    public void dislikeArticle(String userName, String articleTitle){
        DatabaseHandler.addDislikeUserAction(userName, articleTitle);
    }

    public void saveArticle(){
        // get a relative path and copy the file in that path to another given path
    }
}
