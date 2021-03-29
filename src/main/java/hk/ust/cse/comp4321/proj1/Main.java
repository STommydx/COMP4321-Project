package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {

    public static void crawl(URL crawlUrl, String forwardDb, String invertedDb, String lookupDb) {
        Crawler crawler = new Crawler(crawlUrl);
        System.out.println("Crawler now crawling from root URL " + crawlUrl.toString() + "...");
        crawler.crawlLoop();

        // put in databases
        List<DocumentRecord> documentRecordList = crawler.getDocumentRecords();
        System.out.println("Crawler finished crawling. Total of " + documentRecordList.size() + " documents are retrieved.");

        System.out.println("Inserting crawled records to RocksDB tables...");
        try {
            RocksIntegerMap<DocumentRecord> forwardDatabase = new RocksIntegerMap<>(forwardDb);
            RocksStringMap<TreeMap<Integer, Integer>> invertedDatabase = new RocksStringMap<>(invertedDb);
            RocksStringMap<Integer> urlDatabase = new RocksStringMap<>(lookupDb);

            int recordAdded = 0;
            int recordModified = 0;

            for (DocumentRecord documentRecord : documentRecordList) {
                Integer urlKey = urlDatabase.get(documentRecord.getUrl().toString());
                Integer currentKey;

                if (urlKey == null) {  // if url not exist in database
                    // get a new key to for this url/documentRecord and update DB
                    currentKey = forwardDatabase.getNextID();
                    urlDatabase.put(documentRecord.getUrl().toString(), currentKey);
                    recordAdded++;
                } else {
                    currentKey = urlDatabase.get(documentRecord.getUrl().toString());

                    DocumentRecord dbRecord = forwardDatabase.get(currentKey);
                    if (dbRecord.getLastModificationDate().equals(documentRecord.getLastModificationDate())) {
                        // same modification date, do nothing
                        continue;
                    }
                    recordModified++;
                }

                // update forward index and inverted index
                forwardDatabase.put(currentKey, documentRecord);
                for (Map.Entry<String, Integer> item : documentRecord.getFreqTable().entrySet()) {
                    String keyword = item.getKey();
                    TreeMap<Integer, Integer> data = invertedDatabase.get(keyword);
                    // can be empty if keyword is not yet in the inverted index
                    if (data == null) {
                        data = new TreeMap<>();
                    }
                    data.put(currentKey, item.getValue());
                    invertedDatabase.put(keyword, data);
                }
            }
            System.out.println("Successfully inserted all records into RocksDB.");
            System.out.println("Forward Index: " + recordAdded + " added. " + recordModified + " modified.");
        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printRecords(File file, String forwardDb) {
        try (PrintWriter writer = new PrintWriter(file)) {
            RocksStringMap<DocumentRecord> db = new RocksStringMap<>(forwardDb);
            System.out.println("Successfully opened Forward Index table " + forwardDb + ". Reading records...");
            for (RocksStringMap<DocumentRecord>.Iterator it = db.iterator(); it.isValid(); it.next()) {
                writer.println("----------------------");
                writer.println(it.value());
            }
            System.out.println("All records printed in " + file + ".");
        } catch (IOException | ClassNotFoundException | RocksDBException e) {
            e.printStackTrace();
        }
    }

    public static String query(String queryString) {
        try {
            RocksStringMap<TreeMap<Integer, Integer>> invertedDatabase = new RocksStringMap<>("InvertedIndex");
            return invertedDatabase.get(queryString).toString();
        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "Error!";
    }

    public static class CommandOptions {
        @CommandLine.Option(names = {"-c", "--crawl"}, description = "Crawl the web to update database document records")
        boolean shouldCrawl = false;
        @CommandLine.Option(names = {"-u", "--url"}, description = "The root URL to crawl")
        String crawlUrl = "https://www.cse.ust.hk/";
        @CommandLine.Option(names = {"-f", "--forward-index"}, description = "The database name of the forward index")
        String forwardDb = "ForwardIndex";
        @CommandLine.Option(names = {"-i", "--inverted-index"}, description = "The database name of the inverted index")
        String invertedDb = "InvertedIndex";
        @CommandLine.Option(names = {"-l", "--lookup-table"}, description = "The database name of the url to id lookup table")
        String lookupDb = "LookupTable";

        @CommandLine.Option(names = {"-p", "--print"}, description = "Printing database forward index to file")
        boolean shouldPrintRecords = false;
        @CommandLine.Option(names = {"-o", "--output"}, description = "The file to print for printing database records")
        File outputFile = new File("spider_result.txt");

        @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
        boolean usageHelpRequested;
    }

    public static void main(String[] args) {
        CommandOptions commandOptions = new CommandOptions();
        CommandLine commandLine = new CommandLine(commandOptions);
        commandLine.parseArgs(args);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }

        if (commandOptions.shouldCrawl) {
            try {
                URL crawlURL = new URL(commandOptions.crawlUrl);
                crawl(crawlURL, commandOptions.forwardDb, commandOptions.invertedDb,
                        commandOptions.lookupDb);
            } catch (MalformedURLException e) {
                System.out.println("The URL provided (" + commandOptions.crawlUrl + ") is not a valid URL.");
            }
        }

        if (commandOptions.shouldPrintRecords) {
            printRecords(commandOptions.outputFile, commandOptions.forwardDb);
        }
    }

}
