public class Article {
    private String articleID;
    private String Title;
    private String content;
    private String path;
    private String category;
    private int score;    // Based on no. of likes, dislikes and views

    public void storeInDatabase(String title, String content, String category, String path){
        DatabaseHandler.storeAnArticle(title, content, category, path);
    }

    public void displayArticle(){

    }
}
