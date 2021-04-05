package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;

import java.util.Map;
import java.util.Set;

public class BooleanQuery extends Query {
    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) {
        return null;
    }

    @Override
    public Map<Integer, Double> getSimilarityScore(InvertedIndex invertedIndex, Set<Integer> docs) {
        return null;
    }
}
