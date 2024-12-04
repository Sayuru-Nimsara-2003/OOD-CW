public class Article {
    private int articleID;
    private String title;
    private String content;
    private String link;
    private String category;

    public Article(int articleID, String title, String content, String category, String link){
        this.articleID = articleID;
        this.title = title;
        this.content = content;
        this.category = category;
        this.link = link;
    }


    public void setArticleID(int articleID) {this.articleID = articleID;}
    public void setTitle(String title) {this.title = title;}
    public void setContent(String content) {this.content = content;}
    public void setLink(String link) {this.link = link;}
    public void setCategory(String category) {this.category = category;}


    public int getArticleID(){return articleID;}
    public String getTitle(){return title;}
    public String getContent(){return content;}
    public String getLink(){return link;}
    public String getCategory(){return category;}



}
