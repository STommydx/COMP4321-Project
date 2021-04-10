package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.ForwardIndex;
import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class WordQuery extends Query {

    @Override
    public Map<Integer, Double> getSimilarityScore(ForwardIndex forwardIndex, InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Map<String, Double>> documentVectors = getDocumentVectors(forwardIndex, invertedIndex, docs);
        Map<String, Double> queryVector = getQueryVector();
        Map<Integer, Double> similarityScores = new HashMap<>();
        for (int doc : docs) {
            Map<String, Double> v1 = documentVectors.get(doc);
            if (v1 != null)
                similarityScores.put(doc, QueryUtils.cosineSimilarity(v1, queryVector));
        }
        return similarityScores;
    }

    public abstract Map<String, Double> getQueryVector();

    public static Map<Integer, Map<String, Double>> getDocumentVectors(ForwardIndex forwardIndex, InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Set<String> terms = new HashSet<>();
        Map<Integer, Map<String, Double>> termFrequencies = new HashMap<>();
        for (int doc : docs) {
            Map<String, Double> termFrequency = forwardIndex.getNormalizedTfVector(doc);
            terms.addAll(termFrequency.keySet());
            termFrequencies.put(doc, termFrequency);
        }

        Map<String, Double> invertedDocumentFrequencies = new HashMap<>();
        for (String term : terms) {
            invertedDocumentFrequencies.put(term, invertedIndex.getIdf(term));
        }

        return termFrequencies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        x -> DocVectorUtils.elementwiseProduct(x.getValue(), invertedDocumentFrequencies)
                ));
    }

}
