package hk.ust.cse.comp4321.proj1;

import hk.ust.cse.comp4321.proj1.rocks.RocksStringMap;
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

    public double getIdf(String word) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Integer> termFreq = get(word);
        int docFreq = termFreq.values().stream().mapToInt(x -> x > 0 ? 1 : 0).sum();
        return Math.log(1.0 * numOfDocuments / docFreq) / Math.log(2.);
    }

}
