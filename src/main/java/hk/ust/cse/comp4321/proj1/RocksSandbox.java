package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

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

            for (RocksStringMap<ArrayList<String>>.Iterator it = db.iterator(); it.isValid(); it.next()) {
                System.out.println("According to Tommy's DB, we found " + it.key() + " is a " + it.value().toString());
            }

        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            URL projectUrl = new URL("https://course.cse.ust.hk/comp4321/labs/project.html");
            DocumentRecord testPage = new DocumentRecord(projectUrl);
            TreeMap<String, Integer> freqMap = new TreeMap<>();
            freqMap.put("cheesecake", 125);
            freqMap.put("hamburger", 341);
            ArrayList<URL> childLinks = new ArrayList<>();
            childLinks.add(new URL("http://corn-hub.blogspot.com/"));
            childLinks.add(new URL("https://trello.com/b/AnCS980d/ust-spring-2021"));
            testPage.setTitle("COMP4321 Labs : Search Engines for Web and Enterprise Data")
                    .setPageSize(255362)
                    .setLastModificationDate(new Date(1553838304000L))
                    .setFreqTable(freqMap)
                    .setChildLinks(childLinks);
            RocksStringMap<DocumentRecord> indexTest = new RocksStringMap<>("ForwardIndex");
            indexTest.put(projectUrl.toString(), testPage);
            for (RocksStringMap<DocumentRecord>.Iterator it = indexTest.iterator(); it.isValid(); it.next()) {
                System.out.println("----------------------");
                System.out.println(it.value());
            }
        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
