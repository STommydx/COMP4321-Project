package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class RocksIntegerMap<V extends Serializable> extends RocksAbstractMap<Integer, V> {


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
}
