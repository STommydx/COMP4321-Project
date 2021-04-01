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

    private ForwardIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

}
