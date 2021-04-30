package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.ForwardIndex;
import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code Query} class implements the extended boolean model by allowing specification of multiple queries connected
 * by boolean operators (AND, OR)
 */
public class BooleanQuery extends Query {

    enum Operator {
        AND("AND"),
        OR("OR");

        private final String operatorName;

        Operator(String operatorName) {
            this.operatorName = operatorName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return operatorName;
        }
    }

    private final List<Query> queryList;
    private final Operator op;

    /**
     * Constructs a boolean query using a query with operator (AND, OR)
     *
     * @param queryList the query list
     * @param op the operator specified in the query
     */
    public BooleanQuery(List<Query> queryList, Operator op) {
        this.queryList = queryList;
        this.op = op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> set = new HashSet<>();
        for (Query query : queryList) {
            Set<Integer> rootSet = query.getRootSet(invertedIndex);
            set.addAll(rootSet);
        }
        return set;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Double> getSimilarityScore(ForwardIndex forwardIndex, InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        List<Map<Integer, Double>> similarityScoreList = new ArrayList<>();
        for (Query subquery : queryList) {
            similarityScoreList.add(subquery.getSimilarityScore(forwardIndex, invertedIndex, docs));
        }
        Map<Integer, Double> similarityScoreMap = new HashMap<>();
        for (int doc : docs) {
            List<Double> simScoreList = similarityScoreList.stream()
                    .map(x -> x.getOrDefault(doc, 0.))
                    .collect(Collectors.toList());
            similarityScoreMap.put(doc, combineScore(simScoreList));
        }
        return similarityScoreMap;
    }

    private double combineScore(List<Double> scores) {
        if (scores.isEmpty()) return 0.;
        if (op == Operator.AND) {
            double scoreSum = scores.stream().mapToDouble(x -> (1 - x) * (1 - x)).average().orElse(0);
            assert scoreSum >= 0. && scoreSum <= 1.;
            return 1 - Math.sqrt(scoreSum);
        } else if (op == Operator.OR) {
            double scoreSum = scores.stream().mapToDouble(x -> x * x).average().orElse(0.);
            assert scoreSum >= 0. && scoreSum <= 1.;
            return Math.sqrt(scoreSum);
        }
        return 0.;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + queryList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" " + op + " ")) + "]";
    }
}
