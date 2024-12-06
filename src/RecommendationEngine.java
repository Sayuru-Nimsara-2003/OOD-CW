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
import java.util.Objects;
import java.util.Scanner;


public class RecommendationEngine {
    public static final String RESET = "\u001B[0m"; // Reset to default color
    public static final String RED = "\u001B[31m";  // Red text
    public static final String GREEN = "\u001B[32m"; // Green text
    public static final String BLUE = "\u001B[34m"; // Blue text

    public static ArrayList<Integer> recommendArticles(User user) {
        ArrayList<Integer> recommendedArticleIds = new ArrayList<>();

        try {
            File file = new File("Data/ActionScores.csv");

            FileDataModel model = new FileDataModel(file);

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
        System.out.println(BLUE + "\nRecommendations for - " + user.getUserName() + RESET);
        System.out.println(BLUE + "Note - Select article number and enter in the input field\n" + RESET);
        ArrayList<Integer> recommendations = null;

        try {
            recommendations = recommendArticles(user);

            // Check if there are no recommendations
            if (recommendations == null || recommendations.isEmpty()) {
                throw new Exception("No recommendations available.");
            }

            // Display the recommendations if available
            for (int i = 0; i < recommendations.size() ; i++){
                System.out.println((i+1) + ") " + ArticleManager.allArticles.get(recommendations.get(i) - 1 ).getTitle());
            }
        } catch (Exception e) {
            // Handle the case where there are no recommendations
            System.out.println(RED + "No Recommendations due to insufficient user activity" + RESET);
        }

        System.out.println();
        String userInput = "";

        while (true) {
            System.out.println("\nTo go back press B");
            System.out.print(BLUE + "Enter your input: " + RESET);
            userInput = scanner.nextLine();

            if (userInput.matches("\\d+") && Integer.parseInt(userInput) <= Objects.requireNonNull(recommendations).size()) {
                // Direct to articleActions menu
                int selectIndex = recommendations.get((Integer.parseInt(userInput) - 1));
                articleActions(selectIndex, user, scanner);
            } else if (userInput.equalsIgnoreCase("b")) {
                Driver.normalMainMenu(user, scanner);
            } else {
                System.out.println(RED + "Invalid user input. Try again!" + RESET);

            }
        }
    }

    public static void articleActions(int articleID, User user, Scanner scanner) {
        Article article = ArticleManager.allArticles.get(articleID - 1);
        String userInput = "";

        while (true) {
            System.out.println("\nTo view the article, press V");
            System.out.println("Press B to go back to recommendation list");
            System.out.print(BLUE + "  Enter your input : " + RESET);
            userInput = scanner.nextLine();

            UserActions newAction = new UserActions(user);
            if (userInput.equalsIgnoreCase("v")) {
                article.displayArticle();

                // Refresh ActionScore.csv
                DatabaseHandler.generateUserActionsCSV("Data/ActionScores.csv");

                if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "view")) {
                    // If not viewed before add to the database using UserAction class
                    newAction.viewArticle(user.getUserId(), article.getArticleID());
                }

                // Direct to like, unlike and back
                String userInput2 = "";

                while (true) {
                    if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "like")) {
                        System.out.println("\nPress L to like");
                    }
                    if (!newAction.actionAlreadyHappened(user.getUserId(), article.getArticleID(), "dislike")) {
                        System.out.println("Press D to dislike");
                    }
                    System.out.println("Press B to go back to recommendation list");
                    System.out.println(BLUE + "  Enter your input : " + RESET);
                    userInput2 = scanner.nextLine();

                    if (userInput2.equalsIgnoreCase("l")) {
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
                        recommendationMenu(user, scanner);

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

