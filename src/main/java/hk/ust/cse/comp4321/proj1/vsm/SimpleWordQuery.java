package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The {@code SimpleWordQuery} class represent queries with a single keyword.
 * <p>
 * The vector would be a unit vector pointing at the direction of the keyword
 * in the vector space.
 *
 * @see Query
 * @see WordQuery
 * @see MultiWordQuery
 */
public class SimpleWordQuery extends WordQuery {

    private final String word;

    /**
     * Constructs a query using a single query word.
     *
     * @param word the query keyword
     */
    public SimpleWordQuery(String word) {
        this.word = word;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        return invertedIndex.getDocumentsFromWord(word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getQueryVector() {
        Map<String, Double> queryVector = new HashMap<>();
        queryVector.put(word, 1.);
        return queryVector;
    }

    /**
     * Gets the string representation of the query.
     * <p>
     * The method simply returns the query keyword.
     *
     * @return the string representation of the query
     */
    @Override
    public String toString() {
        return word;
    }

}
