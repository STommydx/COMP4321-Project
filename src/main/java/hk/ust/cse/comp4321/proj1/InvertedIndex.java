package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class InvertedIndex extends RocksStringMap<TreeMap<Integer, Integer>> {

    private static final Map<String, InvertedIndex> instances = new HashMap<>();

    public static synchronized InvertedIndex getInstance(String dbName) throws RocksDBException {
        InvertedIndex invertedIndex = instances.get(dbName);
        if (invertedIndex == null) {
            invertedIndex = new InvertedIndex(dbName);
            instances.put(dbName, invertedIndex);
        }
        return invertedIndex;
    }

    private int numOfDocuments = 0;

    private InvertedIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

    public int getNumOfDocuments() {
        return numOfDocuments;
    }

    public void setNumOfDocuments(int numOfDocuments) {
        this.numOfDocuments = numOfDocuments;
    }

    public Set<Integer> getDocumentsFromWord(String word) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Integer> termFreq = get(word);
        return termFreq.keySet();
    }

    public Map<Integer, Double> tfIdf(String word, Set<Integer> documents) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Integer> termFreq = get(word);
        int docFreq = termFreq.values().stream().map(x -> x > 0 ? 1 : 0).reduce(0, Integer::sum);
        double idf = Math.log(1.0 * numOfDocuments / docFreq) / Math.log(2.);
        Map<Integer, Double> tfidf = new HashMap<>();
        for (int doc : documents) {
            tfidf.put(doc, termFreq.getOrDefault(doc, 0) * idf);
        }
        return tfidf;
    }

}
