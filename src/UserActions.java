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
        // Refresh ActionScore.csv
        DatabaseHandler.generateUserActionsCSV("Data/ActionScores.csv");
    }

    public void dislikeArticle(int userID, int articleID){
        DatabaseHandler.addDislikeUserAction(userID, articleID);
        // Refresh ActionScore.csv
        DatabaseHandler.generateUserActionsCSV("Data/ActionScores.csv");
    }

    public boolean actionAlreadyHappened(int userID, int articleID, String action){
        return DatabaseHandler.checkActionExists(userID, articleID, action);
    }

    // Removes the like if dislike action is triggered (if the like action is happened before) and
    // Removes the dislike if like action is triggered (if the dislike action is happened before)
    public void removeAction(int userID, int articleID, String action){
        DatabaseHandler.deleteUserAction(userID, articleID, action);
        // Refresh ActionScore.csv
        DatabaseHandler.generateUserActionsCSV("Data/ActionScores.csv");
    }

}
