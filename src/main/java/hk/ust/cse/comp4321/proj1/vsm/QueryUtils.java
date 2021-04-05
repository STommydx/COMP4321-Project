package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.CrawlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryUtils {

    public static List<String> tokenizeQuery(String query) {
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

    public static List<String> preprocessQuery(List<String> query) {
        return query.stream()
                .filter(CrawlUtils::stopwordFilter)
                .map(CrawlUtils::stemFilter)
                .collect(Collectors.toList());
    }

    public static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        return dot(v1, v2) / l2Norm(v1) / l2Norm(v2);
    }

    public static double dot(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> words = v1.keySet();
        words.retainAll(v2.keySet());
        return words.stream().map(w -> v1.get(w) * v2.get(w)).reduce(0., Double::sum);
    }

    public static double l2Norm(Map<String, Double> v) {
        return Math.sqrt(v.values().stream().map(x -> x * x).reduce(0., Double::sum));
    }

}
