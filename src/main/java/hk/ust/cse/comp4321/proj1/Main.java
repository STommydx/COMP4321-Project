package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Main {

    public static void crawl(String crawlUrl, String forwardDb) {
        Crawler crawler = new Crawler(crawlUrl);
        crawler.crawlLoop();
        System.out.println("\nSuccessfully Returned");

        // put in database
        List<DocumentRecord> documentRecordList = crawler.getDocumentRecords();
        try {
            RocksStringMap<DocumentRecord> db = new RocksStringMap<>(forwardDb);
            for (DocumentRecord documentRecord : documentRecordList) {
                db.put(documentRecord.getUrl().toString(), documentRecord);
            }

        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printRecords(File file, String forwardDb) {
        try (PrintWriter writer = new PrintWriter(file)) {
            RocksStringMap<DocumentRecord> db = new RocksStringMap<>(forwardDb);
            System.out.println("\n-------------document records printing------------------");
            for (RocksStringMap<DocumentRecord>.Iterator it = db.iterator(); it.isValid(); it.next()) {
                writer.println("----------------------");
                writer.println(it.value());
            }
            System.out.println("---------------document records printing finished---------------");
        } catch (IOException | ClassNotFoundException | RocksDBException e) {
            e.printStackTrace();
        }
    }

    public static class CommandOptions {
        @CommandLine.Option(names = {"-c", "--crawl"}, description = "Crawl the web to update database document records")
        boolean shouldCrawl = false;
        @CommandLine.Option(names = {"-u", "--url"}, description = "The root URL to crawl")
        String crawlUrl = "https://www.cse.ust.hk/";
        @CommandLine.Option(names = {"-f", "--forward-index"}, description = "The database name of the forward index")
        String forwardDb = "ForwardIndex";

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
            crawl(commandOptions.crawlUrl, commandOptions.forwardDb);
        }

        if (commandOptions.shouldPrintRecords) {
            printRecords(commandOptions.outputFile, commandOptions.forwardDb);
        }
    }

}
