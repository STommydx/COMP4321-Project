package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.io.Serializable;

/***
 * An object that maps String keys to values. Uses RocksDB for storage.
 * Object serialization code is based on https://stackoverflow.com/a/5837739
 * @param <V> the type of mapped values
 */
public class RocksStringMap<V extends Serializable> extends RocksAbstractMap<String, V> {
    public RocksStringMap(String dbName) throws RocksDBException {
        super(dbName);
    }

    @Override
    protected byte[] keyToBytes(String key) {
        return key.getBytes();
    }

    @Override
    protected String bytesToKey(byte[] bytes) {
        return new String(bytes);
    }
}
