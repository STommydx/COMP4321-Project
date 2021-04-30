package hk.ust.cse.comp4321.proj1;

import hk.ust.cse.comp4321.proj1.rocks.RocksIntegerMap;
import hk.ust.cse.comp4321.proj1.vsm.DocVectorUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ForwardIndex} class represent the forward index table stored in the database
 * which stores the basic details of each document represented by their Document ID
 */
public class ForwardIndex extends RocksIntegerMap<DocumentRecord> {

    private static final Map<String, ForwardIndex> instances = new HashMap<>();
    private static final double TITLE_WEIGHT = 1.2;

    public static synchronized ForwardIndex getInstance(@NotNull String dbName) throws RocksDBException {
        ForwardIndex forwardIndex = instances.get(dbName);
        if (forwardIndex == null) {
            forwardIndex = new ForwardIndex(dbName);
            instances.put(dbName, forwardIndex);
        }
        return forwardIndex;
    }

    @Nullable
    private Integer nextID = null;

    private ForwardIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

    synchronized public Integer getAndIncrementNextID() {
        nextID = getNextID();
        return nextID++;
    }

    synchronized public Integer getNextID() {
        // if next id is not yet fetched
        if (nextID == null) {
            Iterator iterator = new Iterator();
            // if invalid in the first item == empty
            if (!iterator().isValid()) {
                nextID = 0;
            } else {
                // iterator.seekToLast();  // Assuming key grows chronologically: this assumption do not hold!
                int maxId = 0;
                while (iterator.isValid()) {
                    maxId = Math.max(maxId, iterator.key());
                    iterator.next();
                }
                nextID = maxId + 1;
            }
        }
        return nextID;
    }

    /**
     * Insert crawled records to RocksDB document ID lookup table
     *
     * @param docId Document ID
     * @return a map that maps the keyword with its frequency
     * @throws RocksDBException         if there is an error from RocksDB
     * @throws IOException              if there is an I/O problem
     * @throws ClassNotFoundException   if the deserialization fails
     */
    public Map<String, Double> getNormalizedTfVector(int docId) throws RocksDBException, IOException, ClassNotFoundException {
        DocumentRecord documentRecord = get(docId);
        Map<String, Integer> freqTable = documentRecord != null ? documentRecord.getFreqTable() : new HashMap<>();
        Map<String, Integer> titleFreqTable = documentRecord != null ? documentRecord.getTitleFreqTable() : new HashMap<>();

        Map<String, Double> combinedTable = DocVectorUtils.add(
                DocVectorUtils.map(freqTable, x -> 1. * x),
                DocVectorUtils.map(titleFreqTable, x -> TITLE_WEIGHT * x)
        );

        double maxFreq = combinedTable.values().stream().mapToDouble(x -> x).max().orElse(0.);
        return DocVectorUtils.map(combinedTable, x -> x / maxFreq);
    }

}
