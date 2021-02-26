package space.stdx.rockssandbox;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksSandbox {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        String DB_PATH = "db";

        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);

        try {
            RocksDB db = RocksDB.open(options, DB_PATH);

            // Note that all the keys and values must be in byte array format
            byte[] key1 = "key1".getBytes();
            byte[] value1 = "context 1".getBytes();
            byte[] key2 = "key2".getBytes();
            byte[] value2 = "context 2".getBytes();
            byte[] key3 = "key3".getBytes();
            byte[] value3 = "context 3".getBytes();

            // Add some contents into database;
            db.put(key1, value1);
            db.put(key2, value2);
            db.put(key3, value3);

            // Get the content of the “key3” from the database
            System.out.println(new String(db.get(key3)));

            // Remove the triple with key = “key2”
            db.delete(key2);

            // Iterate through all keys
            RocksIterator iter = db.newIterator();

            for(iter.seekToFirst(); iter.isValid(); iter.next())
            {
                // Get and print the content of each key
                System.out.println("iter key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
            }

        } catch (RocksDBException e) {
            e.printStackTrace();
        }

    }
}
