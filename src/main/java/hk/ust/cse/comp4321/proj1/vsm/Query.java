package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.ForwardIndex;
import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Query {

    /**
     * Return a set consisting of ID of documents which contain the interested term(s)
     * @param invertedIndex to get the document ID from a word
     * @return set of docID
     * @throws RocksDBException Rocksdb error
     * @throws IOException Failed to perform IO operation
     * @throws ClassNotFoundException Class not found
     */
    public abstract Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException;

    public abstract Map<Integer, Double> getSimilarityScore(ForwardIndex forwardIndex, InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException;

    public List<Map.Entry<Integer, Double>> query(ForwardIndex forwardIndex, InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> rootSet = getRootSet(invertedIndex);
        Map<Integer, Double> similarityScore = getSimilarityScore(forwardIndex, invertedIndex, rootSet);
        return similarityScore.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toList());
    }

    public static Query parse(@NotNull String query) {
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
            if (queryWord.contains(" "))
                wordQueryList.add(new PhraseQuery(queryWord));
            else
                wordQueryList.add(new SimpleWordQuery(queryWord));
        }
        return new MultiWordQuery(wordQueryList);
    }

}
