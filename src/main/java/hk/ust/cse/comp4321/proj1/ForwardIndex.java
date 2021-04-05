package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

public class ForwardIndex extends RocksIntegerMap<DocumentRecord> {

    private static final Map<String, ForwardIndex> instances = new HashMap<>();

    public static synchronized ForwardIndex getInstance(String dbName) throws RocksDBException {
        ForwardIndex forwardIndex = instances.get(dbName);
        if (forwardIndex == null) {
            forwardIndex = new ForwardIndex(dbName);
            instances.put(dbName, forwardIndex);
        }
        return forwardIndex;
    }

    private Integer nextID = null;

    private ForwardIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

    synchronized public Integer getAndIncrementNextID() {
        getNextID();
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

}
