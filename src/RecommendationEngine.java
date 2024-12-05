import org.apache.mahout.cf.taste.impl.model.file.*;
import org.apache.mahout.cf.taste.impl.neighborhood.*;
import org.apache.mahout.cf.taste.impl.recommender.*;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.*;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.*;
import org.apache.mahout.cf.taste.similarity.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class RecommendationEngine {
    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text

    public static ArrayList<Integer> recommendArticles(User user) {
        ArrayList<Integer> recommendedArticleIds = new ArrayList<>();

        try {
            // File path to the CSV
            File file = new File("src/data.csv");  // Adjust path if necessary

            // Create the FileDataModel and load the data
            FileDataModel model = new FileDataModel(file);

            // Ensure the CSV does not include headers or that they're ignored by the model
            System.out.println("Total number of users in the dataset: " + model.getNumUsers());
            System.out.println("Total number of items in the dataset: " + model.getNumItems());

            // Compute similarity between users using Pearson Correlation (common similarity measure)
            UserSimilarity similarity = new EuclideanDistanceSimilarity(model);

            // Define a neighborhood of 20 nearest neighbors (tune this based on your needs)
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(20, similarity, model);

            // Create a recommender based on user similarity and the neighborhood
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            // Get recommended items for the provided userId
            List<RecommendedItem> recommendations = recommender.recommend(user.getUserId(), 10);

            // Add recommended article IDs to the list
            for (RecommendedItem recommendation : recommendations) {
                recommendedArticleIds.add((int) recommendation.getItemID());
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading the CSV file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("General error occurred in recommendation process");
        }

        return recommendedArticleIds;
    }



    // Recommendations menu
    public static void recommendationMenu(User user, Scanner scanner){
        System.out.println(BLUE + "Recommendations for - " + user.getUserName() + RESET);
        System.out.println();
        System.out.println(BLUE + "Note - Select article number and enter in the input field" + RESET);
        ArrayList<Integer> recommendations = recommendArticles(user);
        int index = 0;
        for (int articleID : recommendations){
            Article relevantArticle = ArticleManager.allArticles.get(articleID - 1);
            index++;
            System.out.println("  " + index + ") " + relevantArticle.getTitle());
        }
        System.out.println();
        System.out.println("To go back press B");
        System.out.print(BLUE + "Enter your input: " + RESET);
        String userInput = "";

        while (true) {
            if (userInput.matches("\\d+") && Integer.parseInt(userInput) <= 10) {
                // Direct to articleActions menu
                articleActions(Integer.parseInt(userInput), user, scanner);
            } else if (userInput.equalsIgnoreCase("b")) {
                Driver.normalMainMenu(user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);
                System.out.print(BLUE + "Enter your input: " + RESET);
                userInput = "";
            }
        }
    }

    public static void articleActions(int articleID, User user, Scanner scanner) {
        Article article = ArticleManager.allArticles.get(articleID - 1);
        String userInput = "";

        while (true) {
            System.out.println("To view the article, press V");
            System.out.println("Press B to go back to recommendation list");
            System.out.print(BLUE + "  Enter your input : " + RESET);
            userInput = scanner.nextLine();

            UserActions newAction = new UserActions(user);
            if (userInput.equalsIgnoreCase("v")) {
                article.displayArticle();

                // Add to the database
                newAction.viewArticle(user.getUserId(), articleID);

                // Direct to like, unlike and back
                String userInput2 = "";

                while (true) {
                    System.out.println("Press L to like");
                    System.out.println("Press D to dislike");
                    System.out.println("Press B to go back to recommendation list");
                    System.out.println(BLUE + "  Enter your input : " + RESET);
                    userInput2 = scanner.nextLine();

                    if (userInput2.equalsIgnoreCase("l")) {
                        // Add to the database
                        newAction.likeArticle(user.getUserId(), articleID);

                        // Go back to article list
                        recommendationMenu(user, scanner);
                    } else if (userInput2.equalsIgnoreCase("d")) {
                        // Add to the database
                        newAction.dislikeArticle(user.getUserId(), articleID);

                        // Go back to article list
                        recommendationMenu(user, scanner);
                    } else if (userInput2.equalsIgnoreCase("b")) {
                        //Go back to article list
                        recommendationMenu(user, scanner);
                    } else {
                        System.out.println(RED + "Invalid user input. Try Again!" + RESET);
                    }
                }
            } else if (userInput.equalsIgnoreCase("b")) {
                recommendationMenu(user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);
            }
        }

    }
}

