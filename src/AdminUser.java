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
        String predictedCategory = "";

        System.out.println(BLUE + "\nNote!!!\n To cancel adding an article input dummy values and enter No afterwards at confirmation" + RESET);

        // Title input
        System.out.print("Enter the title : ");
        title = scanner.nextLine();

        // Check if the article already exists
        if (DatabaseHandler.doesArticleExist(title)){
            System.out.println(RED + "Article already exists" + RESET);
            Driver.adminMainMenu(user, scanner);
            return; // Exit the method if article already exists
        }

        System.out.println(RED + "Important!!!\n When adding the content make sure to add input as a single paragraph,\n" +
                " otherwise the input of the content will become faulty" + RESET);
        System.out.println("Enter the content : ");
        content = scanner.nextLine();

        System.out.print("Enter the link to the article : ");
        link = scanner.nextLine();

        // Categorizing text
        predictedCategory = KeywordExtractor.categorizeText(content);
        System.out.println("Predicted category : " + predictedCategory);

        // Ask the user whether they want to change the category
        System.out.print("If you want to change the category press Y, else N : ");
        String userInput = scanner.nextLine();

        while (!userInput.equalsIgnoreCase("y") && !userInput.equalsIgnoreCase("n")){
            System.out.println(RED + "Invalid input. Press Y or N" + RESET);
            userInput = scanner.nextLine();
        }

        if (userInput.equalsIgnoreCase("y")){
            // Ask the user for the category if they choose to change it
            System.out.print("Enter the category out of AI, Health, Politics, Sports, Technology : ");
            String categoryInput = scanner.nextLine();

            while (true){
                if (categoryInput.equalsIgnoreCase("ai")){
                    category = "AI";
                    break;
                } else if (categoryInput.equalsIgnoreCase("health")) {
                    category = "Health";
                    break;
                } else if (categoryInput.equalsIgnoreCase("sports")) {
                    category = "Sports";
                    break;
                } else if (categoryInput.equalsIgnoreCase("politics")) {
                    category = "Politics";
                    break;
                } else if (categoryInput.equalsIgnoreCase("technology")) {
                    category = "Technology";
                    break;
                } else {
                    System.out.println(RED + "Invalid category. Select one from given categories");
                    System.out.print("Enter the category again (AI, Health, Politics, Sports, Technology): ");
                    categoryInput = scanner.nextLine();
                }
            }
        } else {
            category = predictedCategory;
        }

        // Displaying the summary of the article
        System.out.println(BLUE + "Summary of the article details" + RESET);
        System.out.println("\t title -- " + title);
        System.out.println("\t content --" + content);
        System.out.println("\t link --" + link);
        System.out.println("\t category --" + category);
        System.out.println();

        // Confirmation to add the article
        String confirmation = "";

        while (true){
            System.out.print(BLUE + "Do you want to add this article (Enter Y or N) : " + RESET);
            confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("y")){
                // Add to the database and create an instance in that database
                DatabaseHandler.addNewArticle(title, content, category, link);
                System.out.println(GREEN + "Article added successfully. Redirecting to Main Menu\n");
                Driver.adminMainMenu(user, scanner);
                return; // Exit after adding the article

            } else if (confirmation.equalsIgnoreCase("n")) {
                System.out.println();
                Driver.adminMainMenu(user, scanner);
                return; // Exit if user cancels

            } else {
                System.out.println(RED + "Invalid input, Try again!" + RESET);
            }
        }
    }

}
