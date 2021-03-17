package hk.ust.cse.comp4321.proj1;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.rocksdb.RocksDBException;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The data structure for the crawling queue.
 */
class Link {
    String url;
    int level;

    Link(String url, int level) {
        this.url = url;
        this.level = level;
    }
}

/**
 * This is customized exception for those pages that have been visited before.
 */
class RevisitException
        extends RuntimeException {
    public RevisitException() {
        super();
    }
}

public class Crawler {
    private final HashSet<String> urls;     // the set of urls that have been visited before
    public Vector<Link> todos; // the queue of URLs to be crawled
    private int counter = 0; // to count the number of retrieved pages
    static List<DocumentRecord> dr = new ArrayList<>();

    private static final String DB_NAME = "pagesdb";
    private static final String PHASE1_OUTPUT = "phase1.txt";
    static final int MAX_NUMBER_PAGES = 30; // max page
    private static final int MAX_CRAWL_DEPTH = 100;

    Crawler(String _url) {
        this.todos = new Vector<>();
        this.todos.add(new Link(_url, 1));
        this.urls = new HashSet<>();
    }

    /**
     * Send an HTTP request and analyze the response.
     *
     * @return {Response} res
     * @throws HttpStatusException for non-existing pages
     * @throws IOException
     */
    public Response getResponse(String url) throws HttpStatusException, IOException {
        if (this.urls.contains(url)) {
            throw new RevisitException(); // if the page has been visited, break the function
        }

        // the default body size is 2Mb, to attain unlimited page, use the following.
        // Connection conn = Jsoup.connect(this.url).maxBodySize(0).followRedirects(false);
        Connection conn = Jsoup.connect(url).followRedirects(false);

        /* establish the connection and retrieve the response */
        Response res = conn.execute();

        /* if the link redirects to other place... */
        if (res.hasHeader("location")) {
            String actual_url = res.header("location");
            if (this.urls.contains(actual_url)) {
                throw new RevisitException();
            } else {
                this.urls.add(actual_url);
            }
        } else {
            this.urls.add(url);
        }
        return res;
    }

    /**
     * Use a queue to manage crawl tasks.
     */
    public void crawlLoop() {
        while (!this.todos.isEmpty()) {
            Link focus = this.todos.remove(0);
            // feel free to change the depth limit of the spider.
            if (focus.level > MAX_CRAWL_DEPTH) break; // stop criteria
            if (this.urls.contains(focus.url)) continue;   // ignore pages that has been visited
            if (this.counter >= MAX_NUMBER_PAGES) {  // stop when number of pages exceed the constant
                break;
            } else {
                counter++;
            }

            /* start to crawl on the page */
            try {
                Response returns = this.getResponse(focus.url);
                Response res = returns.bufferUp();
                Document doc = res.parse();

                // Check lang
                String htmlLang = doc.select("html").first().attr("lang");
                String bodyLang = doc.select("body").first().attr("lang");
                String lang = htmlLang + bodyLang;
                if (!lang.toLowerCase().contains("en")) {
                    System.out.printf("\n skipped link= %s\n", focus.url);
                    continue;
                }
                // Check lang end

                Vector<String> words = CrawlUtils.extractWords(doc).parallelStream().filter((item) -> {
                    /*
                     * "check if the code range is outside 0-9, A-Z, a-z is enough
                     */
                    boolean flag = true; // flag if the word is taken
                    for (int i = 0; i < item.length(); ++i) {
                        char chara = item.charAt(i);
                        if ((chara < '0' || chara > '9') && (chara < 'A' || chara > 'Z') && (chara < 'a' || chara > 'z')) {
                            flag = false;
                            break;
                        }
                    }
                    return flag;
                }).filter(this::stopWordFilter).collect(Collectors.toCollection(Vector::new));
//				System.out.println("\nWords:");
//				for(String word: words)
//					System.out.print(word + ", ");

                Vector<String> links = CrawlUtils.extractLinks(doc);
//				System.out.printf("\n\nLinks");
                for (int i = 0; i < links.size(); ++i) {
                    String link = links.get(i);
                    link = CrawlUtils.urlPreprocess(focus.url, link);
                    if (link.contains("cse.ust.hk/")) {
                        links.set(i, link);
//						System.out.println("Link: " + link);
                        this.todos.add(new Link(link, focus.level + 1)); // add links
                    }
                }

                // retrieving data
                String lastModified = res.header("Last-Modified");
                if (lastModified == null || lastModified.equals("")) {
                    lastModified = res.header("Date");
                }
                int size = res.bodyAsBytes().length;

                // count keywords
                TreeMap<String, Integer> freqTable = new TreeMap<>();
                for (String item : words)
                    freqTable.put(item, freqTable.getOrDefault(item, 0) + 1);

                // Calling document record to serialise the retrieved data
                DocumentRecord documentRecord = new DocumentRecord(new URL(focus.url));
                documentRecord.setTitle(res.parse().title());
                documentRecord.setLastModificationDate(new Date(lastModified));
                documentRecord.setFreqTable(freqTable);
                documentRecord.setPageSize(size);

                ArrayList<URL> linksList = links.stream().map(a -> {
                    URL url;
                    try {
                        url = new URL(a);
                    } catch (MalformedURLException e) {
                        System.out.println("exception: " + a);
                        return null;
                    }
                    return url;
                }).collect(Collectors.toCollection(ArrayList::new));
                documentRecord.setChildLinks(linksList);

                dr.add(documentRecord);

            } catch (HttpStatusException e) {
                // e.printStackTrace ();
                System.out.printf("\nLink Error: %s\n", focus.url);
            } catch (SSLHandshakeException e) {
                System.out.printf("\nSSL Error: %s\n", focus.url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RevisitException ignored) {
            } catch (Exception e) {
                System.out.printf("\nUnhandled error: %s\n", e.getMessage());
            }
        }

    }

    /**
     * @param word
     * @return
     */
    private boolean stopWordFilter(String word) {
        //Todo
        return true;
    }

    public static void main(String[] args) {
        String url = "https://www.cse.ust.hk/";
        Crawler crawler = new Crawler(url);
        crawler.crawlLoop();
        System.out.println("\nSuccessfully Returned");

        // put in database
        try {
            RocksStringMap<DocumentRecord> db = new RocksStringMap<>(DB_NAME);
            for (DocumentRecord documentRecord : dr) {
                db.put(documentRecord.getUrl().toString(), documentRecord);
            }

            // write out results to a files
            File file = new File(PHASE1_OUTPUT);
            PrintWriter writer = new PrintWriter(file);
            System.out.println("\n-------------document records printing------------------");
            for (RocksStringMap<DocumentRecord>.Iterator it = db.iterator(); it.isValid(); it.next()) {
                writer.println("----------------------");
                writer.println(it.value());
            }
            writer.close();
            System.out.println("---------------document records printing finished---------------");

        } catch (RocksDBException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

