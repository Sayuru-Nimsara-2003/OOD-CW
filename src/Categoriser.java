import java.util.*;

public abstract class Categoriser {

    private String category;

    public abstract String categorise(String text);
}

class KeywordExtractor extends Categoriser{

        // Map of categories to keywords
        public static final Map<String, List<String>> categoryKeywords = new HashMap<>();

        static {
            // Initializing keywords for each category
            categoryKeywords.put("Health", Arrays.asList("health", "doctor", "medicine", "hospital", "disease", "treatment", "clinic", "die", "bacteri", "disease", "pharm", "health", "drugs", "virus", "obesity", "protein", "nutrit", "vitamin", "food","sleep", "brain", "brain", "mental", "mental"));
            categoryKeywords.put("AI", Arrays.asList("artificial intelligence", "ai","agi", "machine learning", "neural network", "algorithm", "deep learning", "gemini", "openai", "alexa", "ai", "ai", "ai", "large language", "model", "openai", "chatgpt","chat gpt", "ai tools", "chatgpt", "generative ai", "clone", "siri","openai", "chatgpt", "chatgpt", "generative ai", "gauss", "multimodal ai", "chatbot", "chatbot"));
            categoryKeywords.put("Technology", Arrays.asList("ev","tech", "car","rocket","car","rocket","metro","tech", "device", "tiktok", "google", "youtube", "phone", "software", "hardware", "gadgets", "internet", "app", "chip", "electric", "electronic","facebook", "nanotech", "robot", "material"));
            categoryKeywords.put("Politics", Arrays.asList("elect", "government", "policy", "war", "economy", "politic","politician", "president", "congress", "prime minister", "diplomat", "city", "province", "vote", "voting", "parliament", "bill", "state", "senate", "sanction", "russia"));
            categoryKeywords.put("Sports", Arrays.asList("sports", "football", "basketball", "rugby", "league", "cricket", "ball", "chess", "captain", "team", "tournament", "score", "victory", "won", "team", "lost", "cricket", "athlet", "match", "play", "cup", "player"));
        }

        // Method to categorize the text based on keywords
        public static String categorizeText(String text) {
            // Convert text to lowercase
            String normalizedText = text.toLowerCase();

            // Map to store the score for each category
            Map<String, Integer> categoryScores = new HashMap<>();

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
    public String categorise(String text) {
        String category = categorizeText(text);
        return category;
    }
}



// For possible future improvements in categorisation
class NLPModel extends Categoriser{
    @Override
    public String categorise(String text) {
        // categoriser implementation
        return null;
    }
}
