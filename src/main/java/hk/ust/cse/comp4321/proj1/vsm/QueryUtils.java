package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.nlp.NLPUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryUtils {

    public static final String AND_TOKEN = "_AND_";
    public static final String OR_TOKEN = "_OR_";

    public static List<String> tokenizeQuery(@NotNull String query) {
        List<String> tokenizedQuery = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean shouldEscape = false;
        for (char c : query.toCharArray()) {
            if (c == '"') {
                shouldEscape = !shouldEscape;
            } else if (c == ' ') {
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

    public static String extractSpecialTokens(@NotNull String token) {
        if (token.equalsIgnoreCase("AND")) {
            return AND_TOKEN;
        }
        if (token.equalsIgnoreCase("OR")) {
            return OR_TOKEN;
        }
        return token;
    }

    public static List<String> preprocessQuery(List<String> query) {
        return query.stream()
                .map(QueryUtils::extractSpecialTokens)
                .filter(NLPUtils::stopwordFilter)
                .map(NLPUtils::stemFilter)
                .collect(Collectors.toList());
    }

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

    public static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        return dot(v1, v2) / l2Norm(v1) / l2Norm(v2);
    }

    public static double dot(Map<String, Double> v1, Map<String, Double> v2) {
        Map<String, Double> vProduct = DocVectorUtils.elementwiseProduct(v1, v2);
        return vProduct.values().stream().reduce(0., Double::sum);
    }

    public static double l2Norm(Map<String, Double> v) {
        return Math.sqrt(v.values().stream().mapToDouble(x -> x * x).sum());
    }

}
