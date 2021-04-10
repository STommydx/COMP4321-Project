package hk.ust.cse.comp4321.proj1.rocks;

import org.jetbrains.annotations.Nullable;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class RocksAbstractMap<K, V extends Serializable> {

    protected abstract byte[] keyToBytes(K key);
    protected abstract K bytesToKey(byte[] bytes);

    public class Iterator {
        private final RocksIterator it;

        public Iterator() {
            it = db.newIterator();
            it.seekToFirst();
        }

        public void next() {
            it.next();
        }

        public boolean isValid() {
            return it.isValid();
        }

        public K key() {
            return bytesToKey(it.key());
        }

        public @Nullable V value() throws IOException, ClassNotFoundException {
            //noinspection unchecked
            return (V) SerializationUtils.deserialize(it.value());
        }

        public void seekToFirst() {
            it.seekToFirst();
        }

        public void seekToLast(){ it.seekToLast(); }
    }

    private final RocksDB db;
    private static final String ENV_DB_BASE_PATH = "SE_DB_BASE_PATH";
    private static final String DEFAULT_DB_BASE_PATH = "db";

    public RocksAbstractMap(String dbName) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);
        String dbBasePath = System.getenv(ENV_DB_BASE_PATH);
        if (dbBasePath == null) dbBasePath = DEFAULT_DB_BASE_PATH;
        Path path = Paths.get(dbBasePath, dbName);
        File dbFolder = path.toFile();
        if (!dbFolder.exists() && !dbFolder.mkdirs()) {
            System.err.println("Error: Fail to create database folder: " + path);
        }
        db = RocksDB.open(path.toString());
    }

    public @Nullable V get(K key) throws RocksDBException, IOException, ClassNotFoundException {
        byte[] valBytes = db.get(keyToBytes(key));
        //noinspection unchecked
        return (V) SerializationUtils.deserialize(valBytes);
    }

    public @Nullable V put(K key, V value) throws RocksDBException, IOException, ClassNotFoundException {
        V prevValue = get(key);
        db.put(keyToBytes(key), SerializationUtils.serialize(value));
        return prevValue;
    }

    public @Nullable V remove(K key) throws RocksDBException, IOException, ClassNotFoundException {
        V prevValue = get(key);
        db.delete(keyToBytes(key));
        return prevValue;
    }

    public Iterator iterator() {
        return new Iterator();
    }

}
