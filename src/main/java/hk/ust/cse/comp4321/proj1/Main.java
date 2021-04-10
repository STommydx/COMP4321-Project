package hk.ust.cse.comp4321.proj1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hk.ust.cse.comp4321.proj1.rocks.RocksAbstractMap;
import hk.ust.cse.comp4321.proj1.vsm.Query;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String forwardDbName = getValueOrDefault(System.getenv("SE_DB_FORWARD_INDEX"), "ForwardIndex");
    private static final String invertedDbName = getValueOrDefault(System.getenv("SE_DB_INVERTED_INDEX"), "InvertedIndex");
    private static final String lookupDbName = getValueOrDefault(System.getenv("SE_DB_LOOKUP_TABLE"), "LookupTable");

    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static void crawl(URL crawlUrl, String forwardDb, String invertedDb, String lookupDb, int crawlPages, int crawlDepth) {
        Crawler crawler = new Crawler(crawlUrl, crawlPages, crawlDepth);
        System.out.println("Crawler now crawling from root URL " + crawlUrl.toString() + "...");
        crawler.crawlLoop();

        // put in databases
        List<DocumentRecord> documentRecordList = crawler.getDocumentRecords();
        System.out.println("Crawler finished crawling. Total of " + documentRecordList.size() + " documents are retrieved.");

        System.out.println("Inserting crawled records to RocksDB tables...");
        try {
            ForwardIndex forwardDatabase = ForwardIndex.getInstance(forwardDb);
            InvertedIndex invertedDatabase = InvertedIndex.getInstance(invertedDb);
            DocumentLookupIndex urlDatabase = DocumentLookupIndex.getInstance(lookupDb);

            int recordAdded = 0;
            int recordModified = 0;

            for (DocumentRecord documentRecord : documentRecordList) {
                Integer urlKey = urlDatabase.get(documentRecord.getUrl().toString());
                Integer currentKey;

                if (urlKey == null) {  // if url not exist in database
                    // get a new key to for this url/documentRecord and update DB
                    currentKey = forwardDatabase.getAndIncrementNextID();
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
            ForwardIndex db = ForwardIndex.getInstance(forwardDb);
            System.out.println("Successfully opened Forward Index table " + forwardDb + ". Reading records...");
            for (RocksAbstractMap<Integer, DocumentRecord>.Iterator it = db.iterator(); it.isValid(); it.next()) {
                writer.println("----------------------");
                writer.println(it.value());
            }
            System.out.println("All records printed in " + file + ".");
        } catch (IOException | ClassNotFoundException | RocksDBException e) {
            e.printStackTrace();
        }
    }

    public static void queryAndPrint(String queryString, String forwardDb, String invertedDb) {
        System.out.println("Now querying: '" + queryString + "'");
        Query query = Query.parse(queryString);
        System.out.println("Parsed query: '" + query + "'");
        try {
            ForwardIndex forwardIndex = ForwardIndex.getInstance(forwardDb);
            InvertedIndex invertedIndex = InvertedIndex.getInstance(invertedDb);
            invertedIndex.setNumOfDocuments(forwardIndex.getNextID());  // hacky way to get approx. no of documents
            List<Map.Entry<Integer, Double>> rawResult = query.query(forwardIndex, invertedIndex);
            System.out.println("Raw query result: " + rawResult);
            List<QueryResultEntry> result = QueryResultEntry.loadQueryResult(rawResult, forwardIndex, 50);
            System.out.println("Result: " + mapper.writeValueAsString(result));
        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String query(String queryString, String forwardDb, String invertedDb) throws RocksDBException, IOException, ClassNotFoundException {
        Query query = Query.parse(queryString);
        ForwardIndex forwardIndex = ForwardIndex.getInstance(forwardDb);
        InvertedIndex invertedIndex = InvertedIndex.getInstance(invertedDb);
        invertedIndex.setNumOfDocuments(forwardIndex.getNextID());  // hacky way to get approx. no of documents
        List<Map.Entry<Integer, Double>> rawResult = query.query(forwardIndex, invertedIndex);
        List<QueryResultEntry> result = QueryResultEntry.loadQueryResult(rawResult, forwardIndex, 50);
        return mapper.writeValueAsString(result);
    }

    public static String query(String queryString) throws RocksDBException, IOException, ClassNotFoundException {
        return query(queryString, forwardDbName, invertedDbName);
    }

    public static class CommandOptions {
        @CommandLine.Option(names = {"-c", "--crawl"}, description = "Crawl the web to update database document records")
        boolean shouldCrawl = false;
        @CommandLine.Option(names = {"-u", "--url"}, description = "The root URL to crawl")
        String crawlUrl = "https://www.cse.ust.hk/";
        @CommandLine.Option(names = {"-d", "--depth"}, description = "The maximum recursive depth when crawling the pages")
        int crawlDepth = 5;
        @CommandLine.Option(names = {"-n", "--num-docs"}, description = "The maximum number of documents that should be crawled")
        int crawlPages = 30;

        @CommandLine.Option(names = {"-q", "--query"}, description = "The query term for query")
        String queryString = null;
        @CommandLine.Option(names = "--query-interactive", description = "Interactive query mode: input query from standard input")
        boolean interactiveQuery = false;

        @CommandLine.Option(names = {"-f", "--forward-index"}, description = "The database name of the forward index")
        String forwardDb = forwardDbName;
        @CommandLine.Option(names = {"-i", "--inverted-index"}, description = "The database name of the inverted index")
        String invertedDb = invertedDbName;
        @CommandLine.Option(names = {"-l", "--lookup-table"}, description = "The database name of the url to id lookup table")
        String lookupDb = lookupDbName;

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
                        commandOptions.lookupDb, commandOptions.crawlPages, commandOptions.crawlDepth);
            } catch (MalformedURLException e) {
                System.out.println("The URL provided (" + commandOptions.crawlUrl + ") is not a valid URL.");
            }
        }

        if (commandOptions.shouldPrintRecords) {
            printRecords(commandOptions.outputFile, commandOptions.forwardDb);
        }

        if (commandOptions.queryString != null) {
            queryAndPrint(commandOptions.queryString, commandOptions.forwardDb, commandOptions.invertedDb);
        }

        if (commandOptions.interactiveQuery) {
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    queryAndPrint(scanner.nextLine(), commandOptions.forwardDb, commandOptions.invertedDb);
                }
            }
        }

    }

}
