import java.util.ArrayList;
import java.util.Scanner;

public class AdminUser extends User{
    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text
    static ArrayList<Article> allArticles = new ArrayList<Article>();

    public static void addArticlesMenu(User user, Scanner scanner){
        String title = "";
        String content = "";
        String link = "";
        String category = "";

        System.out.println(BLUE + "Note!!!\n To cancel adding an article input dummy values and enter No afterwards at confirmation"+ RESET);
        // title input
        System.out.print("Enter the title : ");
        title = scanner.nextLine();

        System.out.println(RED + "Important!!!\n When adding the content make sure to add input as a single paragraph,\n" +
                " otherwise the input of the content will become faulty" + RESET);
        System.out.println("Enter the content : ");
        content = scanner.nextLine();

        System.out.println("Enter the link to the article : ");
        link = scanner.nextLine();

        String predictedCategory = KeywordExtractor.categorizeText(content);
        System.out.println("Predicted category : " + predictedCategory);
        System.out.print("If you want to change the category press Y, else N");
        String userInput = scanner.nextLine();

        while (!userInput.equalsIgnoreCase("y") && !userInput.equalsIgnoreCase("n")){
            System.out.println(RED + "Invalid input. Press Y or N" + RESET);
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("y")){
                System.out.print("Enter the category out of AI, Health, Politics, Sports, Technology : ");
                String categoryInput = scanner.nextLine();

                while (true){
                    if (categoryInput.equalsIgnoreCase("ai")){
                        category = "AI";
                        return;
                    } else if (categoryInput.equalsIgnoreCase("health")) {
                        category = "Health";
                        return;
                    } else if (categoryInput.equalsIgnoreCase("sports")) {
                        category = "Sports";
                        return;
                    } else if (categoryInput.equalsIgnoreCase("politics")) {
                        category = "Politics";
                        return;
                    } else if (categoryInput.equalsIgnoreCase("technology")) {
                        category = "Technology";
                        return;
                    } else {
                        System.out.println(RED + "Invalid category. Select one from given categories");
                        System.out.print("Enter the category again (AI, Health, Politics, Sports, Technology): ");
                        categoryInput = scanner.nextLine();
                    }
                }

            }
            if (userInput.equalsIgnoreCase("n")){
                category = predictedCategory;
            }
        }

        System.out.println(BLUE + "Summary of the article details" + RESET);
        System.out.println("\t title -- " + title);
        System.out.println("\t content --" + content);
        System.out.println("\t link --" + link);
        System.out.println("\t category --" + category);
        System.out.println();

        String confirmation = "";

        while (true){
            System.out.print("Do you want to add this article (Enter Y or N) : ");
            confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("y")){
                // Add to the database and create an instance in that database
                DatabaseHandler.addNewArticle(title, content, category, link);
                System.out.println(GREEN + "Article added successfully. Redirecting to Main Menu\n");
                Driver.adminMainMenu(user,scanner);

            } else if (confirmation.equalsIgnoreCase("n")) {
                System.out.println();
                Driver.adminMainMenu(user,scanner);
            }else {
                System.out.println(RED + "Invalid input, Try again!" + RESET);
            }
        }



    }
}
