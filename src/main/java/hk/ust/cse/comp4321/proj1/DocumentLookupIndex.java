package hk.ust.cse.comp4321.proj1;

import hk.ust.cse.comp4321.proj1.rocks.RocksStringMap;
import hk.ust.cse.comp4321.proj1.vsm.Query;
import hk.ust.cse.comp4321.proj1.vsm.SimpleWordQuery;
import hk.ust.cse.comp4321.proj1.vsm.WordQuery;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code DocumentLookupIndex} class represent the document ID lookup table stored in the database
 * which stores the document ID of a certain URL
 */
public class DocumentLookupIndex extends RocksStringMap<Integer> {

    private static final Map<String, DocumentLookupIndex> instances = new HashMap<>();

    /**
     * Insert crawled records to RocksDB document ID lookup table
     *
     * @param dbName Database Name
     * @return documentLookupIndex
     * @throws RocksDBException if there is an error from RocksDB
     */
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
