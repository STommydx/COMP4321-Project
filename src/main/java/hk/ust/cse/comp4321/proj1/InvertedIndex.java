package hk.ust.cse.comp4321.proj1;

import hk.ust.cse.comp4321.proj1.rocks.RocksStringMap;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InvertedIndex extends RocksStringMap<TreeMap<Integer, ArrayList<Integer>>> {

    private static final Map<String, InvertedIndex> instances = new HashMap<>();

    public static synchronized InvertedIndex getInstance(@NotNull String dbName) throws RocksDBException {
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
        Map<Integer, ArrayList<Integer>> termLoc = get(word);
        return termLoc != null ? termLoc.keySet() : new HashSet<>();
    }

    public double getIdf(String word) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, ArrayList<Integer>> termLoc = get(word);
        if (termLoc == null) return 0.;
        int docFreq = termLoc.values().stream().map(ArrayList::size).mapToInt(x -> x > 0 ? 1 : 0).sum();
        return Math.log(1.0 * numOfDocuments / docFreq) / Math.log(2.);
    }

    /**
     * Return a set containing positions of the word in a document
     * @param word the word in question
     * @param docID the desired document
     * @return Set of positions
     * @throws RocksDBException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Set<Integer> getWordPosFromDocuments(String word, Integer docID) throws RocksDBException, IOException, ClassNotFoundException {
        TreeMap<Integer, ArrayList<Integer>> postings = this.get(word);
        return postings != null ? new HashSet<>(postings.get(docID)) : new HashSet<>();
    }

}
