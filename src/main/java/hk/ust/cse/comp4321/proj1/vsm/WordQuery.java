package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class WordQuery extends Query {

    @Override
    public Map<Integer, Double> getSimilarityScore(InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Map<String, Double>> documentVectors = getDocumentVectors(invertedIndex, docs);
        Map<String, Double> queryVector = getQueryVector();
        Map<Integer, Double> similarityScores = new HashMap<>();
        for (int doc : docs) {
            similarityScores.put(doc, QueryUtils.cosineSimilarity(documentVectors.get(doc), queryVector));
        }
        return similarityScores;
    }

    public abstract Map<String, Double> getQueryVector();

    public abstract Map<Integer, Map<String, Double>> getDocumentVectors(InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException;

}
