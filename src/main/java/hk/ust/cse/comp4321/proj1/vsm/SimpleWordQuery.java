package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleWordQuery extends WordQuery {

    private final String word;

    public SimpleWordQuery(String word) {
        this.word = word;
    }

    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        return invertedIndex.getDocumentsFromWord(word);
    }

    @Override
    public Map<String, Double> getQueryVector() {
        Map<String, Double> queryVector = new HashMap<>();
        queryVector.put(word, 1.);
        return queryVector;
    }

    @Override
    public Map<Integer, Map<String, Double>> getDocumentVectors(InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Double> tfidf = invertedIndex.tfIdf(word, docs);
        Map<Integer, Map<String, Double>> docVectors = new HashMap<>();
        for (int doc : docs) {
            Map<String, Double> docVector = new HashMap<>();
            docVector.put(word, tfidf.getOrDefault(doc, 0.));
            docVectors.put(doc, docVector);
        }
        return docVectors;
    }

    @Override
    public String toString() {
        return word;
    }

}
