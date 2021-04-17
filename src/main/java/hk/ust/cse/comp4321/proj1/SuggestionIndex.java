package hk.ust.cse.comp4321.proj1;

import hk.ust.cse.comp4321.proj1.rocks.RocksStringMap;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SuggestionIndex extends RocksStringMap<ArrayList<String>> {

    private static final Map<String, SuggestionIndex> instances = new HashMap<>();

    public static synchronized SuggestionIndex getInstance(@NotNull String dbName) throws RocksDBException {
        SuggestionIndex suggestionIndex = instances.get(dbName);
        if (suggestionIndex == null) {
            suggestionIndex = new SuggestionIndex(dbName);
            instances.put(dbName, suggestionIndex);
        }
        return suggestionIndex;
    }

    private SuggestionIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

}
