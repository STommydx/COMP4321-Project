package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;
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

    private InvertedIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

}
