import java.util.ArrayList;

public class ArticleManager {
    private static ArrayList<Article> allArticles = new ArrayList<Article>();

    public void addArticlesToList(Article article){
        allArticles.add(article);
    }

    public static ArrayList<Article> getAllArticles(){
        return allArticles;
    }

    // For displaying article list in UI
    public static void displayAllArticle(){
        int count = 1;
        for (Article article : getAllArticles()){
            System.out.println(count + ") " + article.getTitle());
        }
    }
}