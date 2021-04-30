package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.nlp.NLPUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The {@code QueryUtils} class is an utility class
 * that contains utility methods for handling user's query and similarity score calculation between the query and documents
 * to adopt vector space model in the search engine
 */
public class QueryUtils {

    public static final String AND_TOKEN = "_AND_";
    public static final String OR_TOKEN = "_OR_";

    /**
     * Return a list of strings that stores the tokenized query
     * @param query user's input query
     * @return a list of tokenized query
     */
    public static List<String> tokenizeQuery(@NotNull String query) {
        List<String> tokenizedQuery = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean shouldEscape = false;
        for (char c : query.toCharArray()) {
            if (c == '"') {
                shouldEscape = !shouldEscape;
            } else if (c == ' ' && !shouldEscape) {
                if (buffer.length() > 0)
                    tokenizedQuery.add(buffer.toString());
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
            }
        }
        if (buffer.length() > 0)
            tokenizedQuery.add(buffer.toString());
        return tokenizedQuery;
    }

    /**
     * Identify if the input token is a special token ("AND", "OR")
     * @param token string from the tokenized query
     * @return a string that indicates if the input token is "AND", "OR" or not
     */
    public static String extractSpecialTokens(@NotNull String token) {
        if (token.equalsIgnoreCase("AND")) {
            return AND_TOKEN;
        }
        if (token.equalsIgnoreCase("OR")) {
            return OR_TOKEN;
        }
        return token;
    }

    /**
     * Preprocess the input query by extracting special tokens, stop word removal and stemming
     * @param query user's input query
     * @return a list of strings with preprocessed query
     */
    public static List<String> preprocessQuery(List<String> query) {
        return query.stream()
                .map(QueryUtils::extractSpecialTokens)
                .filter(NLPUtils::stopwordFilter)
                .map(NLPUtils::stemFilter)
                .collect(Collectors.toList());
    }

    /**
     * Split the input the query to multiple subqueries
     * @param query user's input query
     * @param delimiter distinguisher between subqueries (such as "AND", "OR")
     * @return a list of lists of strings which stores the subqueies after splitting the input query
     */
    public static List<List<String>> querySplit(List<String> query, String delimiter) {
        List<List<String>> subQueries = new ArrayList<>();
        List<String> buffer = new ArrayList<>();
        for (String queryWord : query) {
            if (queryWord.equals(delimiter)) {
                if (!buffer.isEmpty()) {
                    subQueries.add(buffer);
                    buffer = new ArrayList<>();
                }
            } else {
                buffer.add(queryWord);
            }
        }
        if (!buffer.isEmpty()) subQueries.add(buffer);
        return subQueries;
    }

    /**
     * Calculate Cosine Similarity between a document and query
     * @param v1 a map between a keyword and weight
     * @param v2 a map between a keyword and weight
     * @return a floating point value after calculating Cosine Similarity
     */
    public static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        return dot(v1, v2) / l2Norm(v1) / l2Norm(v2);
    }

    public static double dot(Map<String, Double> v1, Map<String, Double> v2) {
        Map<String, Double> vProduct = DocVectorUtils.elementwiseProduct(v1, v2);
        return vProduct.values().stream().reduce(0., Double::sum);
    }

    /**
     * Normalize the similarity score by l2 normalization method
     * @param v a map between keyword and weight
     * @return a l2 normalized floating point value
     */
    public static double l2Norm(Map<String, Double> v) {
        return Math.sqrt(v.values().stream().mapToDouble(x -> x * x).sum());
    }

}
