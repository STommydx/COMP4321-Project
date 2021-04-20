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

    /**
     * Get all ID of documents that contains the word
     * @param invertedIndex an InvertedIndex instance
     * @return Set of docID
     * @throws RocksDBException
     * @throws IOException
     * @throws ClassNotFoundException
     */
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
    public String toString() {
        return word;
    }

}
