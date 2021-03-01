package space.stdx.rockssandbox;

import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.ArrayList;

public class RocksSandbox {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        try {
            RocksStringMap<ArrayList<String>> db = new RocksStringMap<>("tommytestdb");

            ArrayList<String> janiceList = new ArrayList<>();
            janiceList.add("beautiful");
            janiceList.add("girl");
            db.put("janice", janiceList);

            ArrayList<String> kelvinList = new ArrayList<>();
            kelvinList.add("handsome");
            kelvinList.add("boy");
            db.put("kelvin", kelvinList);

            System.out.println("According to Tommy's DB, Janice is a " + db.get("janice").toString());
            System.out.println("According to Tommy's DB, Kelvin is a " + db.get("kelvin").toString());

        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
