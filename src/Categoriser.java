import java.util.*;

public abstract class Categoriser {

    private String category;

    public abstract void categorise(String text);
}

class KeywordExtractor extends Categoriser{    // Replace class name with the model name

        // Map of categories to keywords
        public static final Map<String, List<String>> categoryKeywords = new HashMap<>();

        static {
            // Initializing keywords for each category (all lowercase)
            categoryKeywords.put("Health", Arrays.asList("health", "doctor", "medicine", "hospital", "disease", "treatment", "clinic", "die", "bacteri", "disease", "pharm", "health", "drugs", "virus", "obesity", "protein", "nutrit", "vitamin"));
            categoryKeywords.put("AI", Arrays.asList("artificial intelligence", "ai", "machine learning", "neural network", "algorithm", "deep learning", "gemini", "openai", "alexa", "ai", "large language", "model", "openai", "chatgpt", "chatgpt", "generative ai", "clone", "siri","openai", "chatgpt", "chatgpt", "generative ai", "gauss", "multimodal ai", "chatbot", "chatbot"));
            categoryKeywords.put("Technology", Arrays.asList("ev","car","rocket","car","rocket","metro","tech", "innovation", "software", "hardware", "gadgets", "internet", "app", "chip", "electric", "electronic","facebook", "nanotech", "robot", "material"));
            categoryKeywords.put("Politics", Arrays.asList("elect", "government", "policy", "politician", "president", "congress", "prime minister", "diplomat", "city", "province", "vote", "voting", "parliament", "bill", "state", "senate", "sanction", "russia"));
            categoryKeywords.put("Sports", Arrays.asList("sports", "football", "basketball", "team", "tournament", "score", "victory", "lost", "cricket", "athlet", "match", "player", "cup"));
        }

        // Method to categorize the text based on keywords
        public static String categorizeText(String text) {
            // Convert text to lowercase for case-insensitive matching
            String normalizedText = text.toLowerCase();

            // Map to store the score for each category
            Map<String, Integer> categoryScores = new HashMap<>();

            // Iterate over categories and keywords to calculate scores
            for (String category : categoryKeywords.keySet()) {
                int score = 0;
                for (String keyword : categoryKeywords.get(category)) {
                    // Check if the keyword is present in the text (case-insensitive)
                    if (normalizedText.contains(keyword.toLowerCase())) {
                        score++;
                    }
                }
                categoryScores.put(category, score);
            }

            // Find the category with the highest score
            String selectedCategory = null;
            int maxScore = -1;

            for (Map.Entry<String, Integer> entry : categoryScores.entrySet()) {
                if (entry.getValue() > maxScore) {
                    maxScore = entry.getValue();
                    selectedCategory = entry.getKey();
                }
            }

            return selectedCategory;
        }

    @Override
    public void categorise(String text) {
        // Categorize the text
        String category = categorizeText(text);
        System.out.println("The text is categorized as: " + category);
    }
}



// For possible future improvements in categorisation
class NLPModel extends Categoriser{
    @Override
    public void categorise(String text) {
        // categoriser implementation
    }
}
