package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

public class DocumentLookupIndex extends RocksStringMap<Integer> {

    private static final Map<String, DocumentLookupIndex> instances = new HashMap<>();

    public static synchronized DocumentLookupIndex getInstance(String dbName) throws RocksDBException {
        DocumentLookupIndex documentLookupIndex = instances.get(dbName);
        if (documentLookupIndex == null) {
            documentLookupIndex = new DocumentLookupIndex(dbName);
            instances.put(dbName, documentLookupIndex);
        }
        return documentLookupIndex;
    }

    private DocumentLookupIndex(String dbName) throws RocksDBException {
        super(dbName);
    }

}
