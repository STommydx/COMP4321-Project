package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanQuery extends Query {

    enum Operator {
        AND("AND"),
        OR("OR");

        private final String operatorName;

        Operator(String operatorName) {
            this.operatorName = operatorName;
        }

        @Override
        public String toString() {
            return operatorName;
        }
    }

    private final List<Query> queryList;
    private final Operator op;

    public BooleanQuery(List<Query> queryList, Operator op) {
        this.queryList = queryList;
        this.op = op;
    }

    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> set = new HashSet<>();
        for (Query query : queryList) {
            Set<Integer> rootSet = query.getRootSet(invertedIndex);
            set.addAll(rootSet);
        }
        return set;
    }

    @Override
    public Map<Integer, Double> getSimilarityScore(InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        List<Map<Integer, Double>> similarityScoreList = new ArrayList<>();
        for (Query subquery : queryList) {
            similarityScoreList.add(subquery.getSimilarityScore(invertedIndex, docs));
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
            double scoreSum = scores.stream().map(x -> (1 - x) * (1 - x)).reduce(0., Double::sum)
                    / scores.size();
            assert scoreSum >= 0. && scoreSum <= 1.;
            return 1 - Math.sqrt(scoreSum);
        } else if (op == Operator.OR) {
            double scoreSum = scores.stream().map(x -> x * x).reduce(0., Double::sum) / scores.size();
            assert scoreSum >= 0. && scoreSum <= 1.;
            return Math.sqrt(scoreSum);
        }
        return 0.;
    }

    @Override
    public String toString() {
        return "[" + queryList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" " + op + " ")) + "]";
    }
}
