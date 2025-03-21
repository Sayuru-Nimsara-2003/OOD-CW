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
            System.out.println("\nSelect the category");
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
                articlesMenu("AI", user, scanner);
            } else if (userInput.equalsIgnoreCase("h")) {
                // Display Health articles
                articlesMenu("Health", user, scanner);
            } else if (userInput.equalsIgnoreCase("p")){
                //Display politics articles
                articlesMenu("Politics", user, scanner);
            } else if (userInput.equalsIgnoreCase("s")){
                //Display Sport articles
                articlesMenu("Sports", user, scanner);
            } else if (userInput.equalsIgnoreCase("t")) {
                //Display technology articles
                articlesMenu("Technology", user, scanner);
            } else if (userInput.equalsIgnoreCase("b")) {
                Driver.normalMainMenu(user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);
            }
        }
    }

    public static ArrayList<Integer> displayArticlesByCategory(String category) {
        ArrayList<Integer> instanceIndexList = new ArrayList<>();
        int count = 0; // Counter for display numbering

        for (int i = 0; i < allArticles.size(); i++) {
            Article article = allArticles.get(i);
            if (article.getCategory().equals(category)) {
                System.out.println("\t" + ++count + ") " + article.getTitle());
                instanceIndexList.add(i);
            }
        }
        return instanceIndexList;
    }


    public static void articlesMenu(String category, User user, Scanner scanner){
        String userInput = "";

        while (true){
            System.out.println(BLUE + "\nEnter the article number below input field" + RESET);
            System.out.println();
            ArrayList<Integer> articleInstanceIndexes = displayArticlesByCategory(category);
            System.out.println();
            System.out.println("  Press B to go back to category menu");
            System.out.print(BLUE + "Enter your input : " + RESET);
            userInput = scanner.nextLine();

            if (userInput.matches("\\d+") && Integer.parseInt(userInput) <= articleInstanceIndexes.size()){
                int articleInstanceIndex = articleInstanceIndexes.get(Integer.parseInt(userInput) - 1);
                // Direct to articleActions menu with object index
                articleActions(articleInstanceIndex, category, user, scanner);
            } else if (userInput.equalsIgnoreCase("b")) {
                // Goes back to category menu
                displayArticlesMenu(user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);
            }
        }
    }

    public static void articleActions(int articleIndex, String category, User user, Scanner scanner){
        Article article = allArticles.get(articleIndex);
        String userInput = "";

        while (true){
            System.out.println("\nTo view the article, press V");
            System.out.println("Press B to go back to article list");
            System.out.print(BLUE + "  Enter your input : " + RESET);
            userInput = scanner.nextLine();

            UserActions newAction = new UserActions(user);
            if (userInput.equalsIgnoreCase("v")){
                article.displayArticle();

                // Refresh ActionScore.csv
                DatabaseHandler.generateUserActionsCSV("Data/ActionScores.csv");

                if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "view")) {
                    // If not viewed before add to the database using UserAction class
                    newAction.viewArticle(user.getUserId(), article.getArticleID());
                }


                String userInput2 = "";

                while (true){
                    if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "like")) {
                        System.out.println("\nPress L to like");
                    }
                    if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "dislike")) {
                        System.out.println("Press D to dislike");
                    }
                    System.out.println("Press B to go back to article list");
                    System.out.print(BLUE + "  Enter your input : " + RESET);
                    userInput2 = scanner.nextLine();

                    if (userInput2.equalsIgnoreCase("l")){
                        if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "like")){
                            // Add to the database using UserAction class
                            newAction.likeArticle(user.getUserId(), article.getArticleID());

                            //Also remove the dislike if exists
                            if (newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(),"dislike")){
                                newAction.removeAction(user.getUserId(), article.getArticleID(),"dislike" );
                            }
                            System.out.println(GREEN + "You have liked successfully" + RESET);

                        } else {
                            System.out.println(RED + "You have already liked before!" + RESET);
                        }

                        // Go back to article list
                        articlesMenu(category,user,scanner);

                    } else if (userInput2.equalsIgnoreCase("d")) {
                        if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "dislike")) {
                            // Add to the database using UserAction class
                            newAction.dislikeArticle(user.getUserId(), article.getArticleID());

                            // Also remove the like if exists
                            if (newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(),"like")){
                                newAction.removeAction(user.getUserId(), article.getArticleID(),"like" );
                            }
                            System.out.println(GREEN + "You have disliked successfully" + RESET);

                        } else {
                            System.out.println(RED + "You have already disliked before!");
                        }

                        // Go back to article list
                        articlesMenu(category,user,scanner);

                    } else if (userInput2.equalsIgnoreCase("b")) {
                        //Go back to article list
                        articlesMenu(category,user,scanner);
                    } else {
                        System.out.println(RED + "Invalid user input. Try Again!" + RESET);
                    }
                }
            } else if (userInput.equalsIgnoreCase("b")) {
                articlesMenu(category, user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);
            }
        }
    }





}