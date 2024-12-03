public class Article {
    private String articleID;
    private String title;
    private String content;
    private String path;
    private String category;

    public void storeInDatabase(String title, String content, String category, String path){
        DatabaseHandler.storeAnArticle(title, content, category, path);
    }

    public String getArticleID(){return articleID;}
    public String getTitle(){return title;}
    public String getContent(){return content;}
    public String getPath(){return path;}
    public String getCategory(){return category;}


    public void displayArticles(){


    }
}
