import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Scanner;

public class ArticleManager {

    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text
    static ArrayList<Article> allArticles = new ArrayList<Article>();


    // For displaying article list in UI
    public static void displayArticlesMenu(User user, Scanner scanner){
        String userInput;

        while (true){
            System.out.println("Select the category");
            System.out.println("\tPress A for AI");
            System.out.println("\tPress H for Health");
            System.out.println("\tPress P for Politics");
            System.out.println("\tPress S for Sports");
            System.out.println("\tPress T for Technology");
            System.out.println("\tPress B for go back");
            System.out.print(BLUE + "  Enter your input : " + RESET);
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("a")){
                // Display AI articles
            } else if (userInput.equalsIgnoreCase("h")) {
                // Display Health articles
            } else if (userInput.equalsIgnoreCase("p")){
                //Display politics articles
            } else if (userInput.equalsIgnoreCase("s")){
                //Display Sport articles
            } else if (userInput.equalsIgnoreCase("t")) {
                //Display technology articles
            } else if (userInput.equalsIgnoreCase("b")) {
                Driver.normalMainMenu(user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);
            }
        }
    }

    public int displayArticlesByCategory(String category){
        int count = 0;
        int index = 1;
        for (Article article : allArticles){
            if (article.getCategory().equals(category)){
                System.out.println("\t"+ index + ") " + article.getTitle());
                count++;
                index++;
            }
        }
        return count;
    }

    public void articlesMenu(String category, Scanner scanner){
        String userInput = "";

        while (true){
            System.out.println(BLUE + "Enter the article number below input field");
            System.out.println();
            int articleCount = displayArticlesByCategory(category);
            System.out.println();
            System.out.println("  Press B to go back to category menu");
            System.out.print(BLUE + "Enter your input : " + RESET);
            userInput = scanner.nextLine();

            int articleNumber =
            for (int i = 1; i <= articleCount; i++){
                if ((int)userInput == i){

                }
            }
        }
    }


}