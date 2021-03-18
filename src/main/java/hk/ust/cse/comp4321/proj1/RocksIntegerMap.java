package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class RocksIntegerMap<V extends Serializable> extends RocksAbstractMap<Integer, V> {

    // NOT THREAD SAFE
    private Integer nextID = null;

    public RocksIntegerMap(String dbName) throws RocksDBException {
        super(dbName);
    }

    @Override
    protected byte[] keyToBytes(Integer key) {
        return ByteBuffer.allocate(4).putInt(key).array();
    }

    @Override
    protected Integer bytesToKey(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();
    }

    public Integer getNextID(){
        // if next id is not yet fetched
        if (nextID == null){
            Iterator iterator = new Iterator();
            // if invalid in the first item == empty
            if (!iterator().isValid()){
                nextID = 1;
                return 0;
            } else {
                iterator.seekToLast();  // Assuming key grows chronologically
                nextID = iterator.key() + 2;
                return nextID + 1;
            }
        } else {
            return nextID++;
        }
    }
}
