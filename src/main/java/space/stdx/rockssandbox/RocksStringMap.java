package space.stdx.rockssandbox;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/***
 * An object that maps String keys to values. Uses RocksDB for storage.
 * Object serialization code is based on https://stackoverflow.com/a/5837739
 * @param <V> the type of mapped values
 */
public class RocksStringMap<V extends Serializable> {

    private final RocksDB db;
    private static final String DB_BASE_PATH = "db";

    public RocksStringMap(String dbName) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);
        Path path = Paths.get(DB_BASE_PATH, dbName);
        db = RocksDB.open(path.toString());
    }

    public V get(String key) throws RocksDBException, IOException, ClassNotFoundException {
        byte[] valBytes = db.get(key.getBytes());
        //noinspection unchecked
        return (V) SerializationUtils.deserialize(valBytes);
    }

    public V put(String key, V value) throws RocksDBException, IOException, ClassNotFoundException {
        V prevValue = get(key);
        db.put(key.getBytes(), SerializationUtils.serialize(value));
        return prevValue;
    }

}
