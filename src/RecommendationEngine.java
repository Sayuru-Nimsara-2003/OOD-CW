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
import java.util.List;


public class RecommendationEngine {

    public static void recommendArticles(User user) {
        try {
            // File path to the CSV (ensure it's in the correct directory)
            File file = new File("src/data.csv");  // Adjust path if necessary

            // Create the FileDataModel and load the data (ensure your CSV is properly formatted)
            FileDataModel model = new FileDataModel(file);

            // Ensure the CSV does not include headers or that they're ignored by the model
            System.out.println("Total number of users in the dataset: " + model.getNumUsers());
            System.out.println("Total number of items in the dataset: " + model.getNumItems());

            // Compute similarity between users using Pearson Correlation (common similarity measure)
            UserSimilarity similarity = new EuclideanDistanceSimilarity(model);


            // Define a neighborhood of 10 nearest neighbors (tune this based on your needs)
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(20, similarity, model);

            // Create a recommender based on user similarity and the neighborhood
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List<RecommendedItem> recommendations = recommender.recommend(21, 10);
            if (recommendations.isEmpty()) {
                System.out.println("No recommendations for User 1.");
            } else {
                for (RecommendedItem recommendation : recommendations) {
                    System.out.println("Article ID: " + recommendation.getItemID() + " with score: " + recommendation.getValue());
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading the CSV file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("General error occurred in recommendation process");
        }
    }
}
