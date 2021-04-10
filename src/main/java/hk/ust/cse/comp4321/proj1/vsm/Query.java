package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Query {

    public abstract Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException;

    public abstract Map<Integer, Double> getSimilarityScore(InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException;

    public List<Map.Entry<Integer, Double>> query(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> rootSet = getRootSet(invertedIndex);
        Map<Integer, Double> similarityScore = getSimilarityScore(invertedIndex, rootSet);
        return similarityScore.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toList());
    }

    public static Query parse(String query) {
        List<String> tokenizeQuery = QueryUtils.tokenizeQuery(query);
        List<String> preprocessedQuery = QueryUtils.preprocessQuery(tokenizeQuery);
        return parse(preprocessedQuery);
    }

    public static Query parse(List<String> query) {
        // parse OR operator
        List<List<String>> orSubRawQueries = QueryUtils.querySplit(query, QueryUtils.OR_TOKEN);
        if (orSubRawQueries.size() >= 2) {
            List<Query> orSubQueries = orSubRawQueries.stream()
                    .map(Query::parse)
                    .collect(Collectors.toList());
            return new BooleanQuery(orSubQueries, BooleanQuery.Operator.OR);
        }

        // parse AND operator
        List<List<String>> andSubRawQueries = QueryUtils.querySplit(query, QueryUtils.AND_TOKEN);
        if (andSubRawQueries.size() >= 2) {
            List<Query> andSubQueries = andSubRawQueries.stream()
                    .map(Query::parse)
                    .collect(Collectors.toList());
            return new BooleanQuery(andSubQueries, BooleanQuery.Operator.AND);
        }

        List<WordQuery> wordQueryList = new ArrayList<>();
        for (String queryWord : query) {
            // TODO: parse as phase query if word is phrase
            wordQueryList.add(new SimpleWordQuery(queryWord));
        }
        return new MultiWordQuery(wordQueryList);
    }

}
