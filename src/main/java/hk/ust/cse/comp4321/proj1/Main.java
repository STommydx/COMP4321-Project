package hk.ust.cse.comp4321.proj1;

import com.fasterxml.jackson.databind.ObjectMapper;
import hk.ust.cse.comp4321.proj1.rocks.RocksAbstractMap;
import hk.ust.cse.comp4321.proj1.vsm.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String forwardDbName = getValueOrDefault(System.getenv("SE_DB_FORWARD_INDEX"), "ForwardIndex");
    private static final String invertedDbName = getValueOrDefault(System.getenv("SE_DB_INVERTED_INDEX"), "InvertedIndex");
    private static final String lookupDbName = getValueOrDefault(System.getenv("SE_DB_LOOKUP_TABLE"), "LookupTable");
    private static final String suggestDbName = getValueOrDefault(System.getenv("SE_DB_SUGGEST_INDEX"), "SuggestIndex");

    @NotNull
    public static <T> T getValueOrDefault(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static void crawl(URL crawlUrl, String forwardDb, String invertedDb, String lookupDb, String suggestDb, int crawlPages, int crawlDepth) {
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

            // store inverted index updates in memory first
            Map<String, TreeMap<Integer, ArrayList<Integer>>> invertedIndexUpdates = new HashMap<>();

            System.out.println("Putting parent links into document records...");
            Map<URL, DocumentRecord> memoryLookUpTable = documentRecordList.stream().collect(Collectors.
                    toMap(DocumentRecord::getUrl, x -> x));

            for (DocumentRecord documentRecord : documentRecordList) {
                List<URL> childLinks = documentRecord.getChildLinks();
                for (URL link : childLinks) {
                    DocumentRecord dr = memoryLookUpTable.get(link);
                    if (dr == null) continue;
                    dr.addParentLinks(documentRecord.getUrl());
                }
            }

            System.out.println("Parent links put into document records successfully");

            for (DocumentRecord documentRecord : documentRecordList) {
                Integer urlKey = urlDatabase.get(documentRecord.getUrl().toString());
                Integer currentKey;

                if (urlKey == null) {  // if url not exist in database
                    // get a new key to for this url/documentRecord and update DB
                    currentKey = forwardDatabase.getAndIncrementNextID();
                    urlDatabase.put(documentRecord.getUrl().toString(), currentKey);
                    recordAdded++;
                } else {
                    currentKey = urlKey;
                    DocumentRecord dbRecord = forwardDatabase.get(currentKey);
                    if (dbRecord != null && dbRecord.getLastModificationDate().equals(documentRecord.getLastModificationDate())) {
                        // same modification date, do nothing
                        continue;
                    }
                    recordModified++;
                }

                // update forward index
                forwardDatabase.put(currentKey, documentRecord);

                // batch update inverted index with word location
                TreeMap<String, ArrayList<Integer>> words = documentRecord.getWordsWithLoc();
                for (Map.Entry<String, ArrayList<Integer>> word : words.entrySet()) {
                    String keyword = word.getKey();
                    TreeMap<Integer, ArrayList<Integer>> data = invertedIndexUpdates.getOrDefault(keyword, new TreeMap<>());
                    data.put(currentKey, word.getValue());
                    invertedIndexUpdates.put(keyword, data);
                }
            }

            System.out.println("Successfully inserted all records into Forward Index.");

            // update inverted index
            int invertedAdded = 0;
            int invertedModified = 0;
            for (Map.Entry<String, TreeMap<Integer, ArrayList<Integer>>> entry : invertedIndexUpdates.entrySet()) {
                TreeMap<Integer, ArrayList<Integer>> data = invertedDatabase.get(entry.getKey());
                if (data != null) {
                    data.putAll(entry.getValue());
                    invertedDatabase.put(entry.getKey(), data);
                    invertedModified++;
                } else {
                    invertedDatabase.put(entry.getKey(), entry.getValue());
                    invertedAdded++;
                }
            }

            System.out.println("Successfully inserted all records into RocksDB.");
            System.out.println("Forward Index: " + recordAdded + " added. " + recordModified + " modified.");
            System.out.println("Inverted Index: " + invertedAdded + " added. " + invertedModified + " modified.");

            if (suggestDb != null) {
                System.out.println("Building suggestion index...");
                SuggestionTrie suggestionTrie = new SuggestionTrie();
                for (Map.Entry<String, TreeMap<Integer, ArrayList<Integer>>> entry : invertedIndexUpdates.entrySet()) {
                    suggestionTrie.put(entry.getKey(), entry.getValue().size());
                }
                suggestionTrie.buildSuggestion(20);
                Map<String, SuggestionTrie.Node> res = suggestionTrie.getAll();
                SuggestionIndex suggestionIndex = SuggestionIndex.getInstance(suggestDb);
                for (Map.Entry<String, SuggestionTrie.Node> entry : res.entrySet()) {
                    String k = entry.getKey();
                    SuggestionTrie.Node v = entry.getValue();
                    suggestionIndex.put(k, new ArrayList<>(v.getSuggestions().keySet()));
                }
                System.out.println("Successfully built suggestion index.");
            }
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

    public static List<QueryResultEntry> queryRaw(@NotNull String queryString, String forwardDb, String invertedDb) throws RocksDBException, IOException, ClassNotFoundException {
        Query query = Query.parse(queryString);
        ForwardIndex forwardIndex = ForwardIndex.getInstance(forwardDb);
        InvertedIndex invertedIndex = InvertedIndex.getInstance(invertedDb);
        invertedIndex.setNumOfDocuments(forwardIndex.getNextID());  // hacky way to get approx. no of documents
        List<Map.Entry<Integer, Double>> rawResult = query.query(forwardIndex, invertedIndex);
        return QueryResultEntry.loadQueryResult(rawResult, forwardIndex, 50);
    }

    public static List<QueryResultEntry> queryRaw(@NotNull String queryString) throws RocksDBException, IOException, ClassNotFoundException {
        return queryRaw(queryString, forwardDbName, invertedDbName);
    }

    public static String query(@NotNull String queryString, String forwardDb, String invertedDb) throws RocksDBException, IOException, ClassNotFoundException {
        List<QueryResultEntry> resultEntries = queryRaw(queryString, forwardDb, invertedDb);
        return mapper.writeValueAsString(resultEntries);
    }

    public static String query(@NotNull String queryString) throws RocksDBException, IOException, ClassNotFoundException {
        return query(queryString, forwardDbName, invertedDbName);
    }

    public static String suggest(@NotNull String queryString, String suggestDb) throws RocksDBException, IOException, ClassNotFoundException {
        int spaceIdx = queryString.lastIndexOf(" ");
        SuggestionIndex suggestionIndex = SuggestionIndex.getInstance(suggestDb);
        if (spaceIdx == -1) {
            List<String> suggestions = suggestionIndex.get(queryString);
            if (suggestions == null) suggestions = new ArrayList<>();
            return mapper.writeValueAsString(suggestions);
        } else {
            String prefix = queryString.substring(0, spaceIdx + 1);
            String suffix = queryString.substring(spaceIdx + 1);
            List<String> suggestions = suggestionIndex.get(suffix);
            if (suggestions == null) suggestions = new ArrayList<>();
            suggestions = suggestions.stream().map(x -> prefix + x).collect(Collectors.toList());
            return mapper.writeValueAsString(suggestions);
        }
    }

    public static String suggest(@NotNull String queryString) throws RocksDBException, IOException, ClassNotFoundException {
        return suggest(queryString, suggestDbName);
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
        @Nullable String queryString = null;
        @CommandLine.Option(names = {"-Q", "--query-interactive"}, description = "Interactive query mode: input query from standard input")
        boolean interactiveQuery = false;

        @CommandLine.Option(names = {"-f", "--forward-index"}, description = "The database name of the forward index")
        String forwardDb = forwardDbName;
        @CommandLine.Option(names = {"-i", "--inverted-index"}, description = "The database name of the inverted index")
        String invertedDb = invertedDbName;
        @CommandLine.Option(names = {"-l", "--lookup-table"}, description = "The database name of the url to id lookup table")
        String lookupDb = lookupDbName;
        @CommandLine.Option(names = {"-s", "--suggestion-index"}, description = "The database name of the suggestion index")
        String suggestDb = suggestDbName;

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
                        commandOptions.lookupDb, commandOptions.suggestDb, commandOptions.crawlPages,
                        commandOptions.crawlDepth);
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
