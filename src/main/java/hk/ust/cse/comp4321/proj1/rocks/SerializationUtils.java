package hk.ust.cse.comp4321.proj1.rocks;

import org.jetbrains.annotations.Nullable;

import java.io.*;

public class SerializationUtils {

    public static byte[] serialize(Object o) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(o);
            }
            return bos.toByteArray();
        }
    }

    @Nullable
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) return null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                return ois.readObject();
            }
        }
    }

}
