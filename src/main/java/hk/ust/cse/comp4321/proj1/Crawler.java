package hk.ust.cse.comp4321.proj1;

import hk.ust.cse.comp4321.proj1.nlp.NLPUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The data structure for the crawling queue.
 */
class Link {
    URL url;
    int level;

    Link(URL url, int level) {
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
    private final URL rootURL;

    private final HashSet<URL> urls;     // the set of urls that have been visited before
    public Vector<Link> todos; // the queue of URLs to be crawled
    private int counter = 0; // to count the number of retrieved pages
    private final List<DocumentRecord> documentRecords = new ArrayList<>();

    private final int maxPages; // max page
    private final int maxDepth;

    Crawler(URL rootURL, int maxPages, int maxDepth) {
        this.rootURL = rootURL;
        this.todos = new Vector<>();
        this.todos.add(new Link(rootURL, 1));
        this.urls = new HashSet<>();
        this.maxPages = maxPages;
        this.maxDepth = maxDepth;
    }

    /**
     * Send an HTTP request and analyze the response.
     *
     * @return {Response} res
     * @throws HttpStatusException for non-existing pages
     * @throws IOException
     */
    public Response getResponse(URL url) throws HttpStatusException, IOException {
        if (this.urls.contains(url)) {
            throw new RevisitException(); // if the page has been visited, break the function
        }

        // the default body size is 2Mb, to attain unlimited page, use the following.
        // Connection conn = Jsoup.connect(this.url).maxBodySize(0).followRedirects(false);
        Connection conn = Jsoup.connect(url.toString()).followRedirects(false);

        /* establish the connection and retrieve the response */
        Response res = conn.execute();

        /* if the link redirects to other place... */
        if (res.hasHeader("location")) {
            URL actual_url = new URL(url, res.header("location"));
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
        while (!todos.isEmpty()) {
            Link focus = todos.remove(0);
            if (focus.level > maxDepth) break; // stop criteria
            if (urls.contains(focus.url)) continue;   // ignore pages that has been visited
            if (counter >= maxPages) {  // stop when number of pages checked out exceed the constant
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

                Vector<String> words = CrawlUtils.extractWords(doc).stream()
                        .filter(NLPUtils::isAlphaNumeric)
                        .filter(NLPUtils::stopwordFilter)
                        .map(NLPUtils::stemFilter)
                        .collect(Collectors.toCollection(Vector::new));

                Vector<String> extractedLinks = CrawlUtils.extractLinks(doc);
                Set<URL> linkSet = new HashSet<>();
                for (String extractedLink : extractedLinks) {
                    URL link = CrawlUtils.urlPreprocess(focus.url, extractedLink);
                    if (link.getHost().equals(rootURL.getHost())) {
                        todos.add(new Link(link, focus.level + 1)); // add links
                        linkSet.add(link);
                    }
                }
                ArrayList<URL> linksList = new ArrayList<>(linkSet);

                // retrieving data
                String lastModified = res.header("Last-Modified");
                if (lastModified == null || lastModified.equals("")) {
                    lastModified = res.header("Date");
                }
                int size = res.bodyAsBytes().length;

                // count keywords
                TreeMap<String, Integer> freqTable = new TreeMap<>();
                for (String item : words) {
                    freqTable.put(item, freqTable.getOrDefault(item, 0) + 1);
                }

                // Calling document record to serialise the retrieved data
                DocumentRecord documentRecord = new DocumentRecord(focus.url);
                documentRecord.setTitle(res.parse().title());
                documentRecord.setLastModificationDate(new Date(lastModified));
                documentRecord.setFreqTable(freqTable);
                documentRecord.setPageSize(size);
                documentRecord.setChildLinks(linksList);
                documentRecord.setWords(words);

                documentRecords.add(documentRecord);

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


    public List<DocumentRecord> getDocumentRecords() {
        return documentRecords;
    }

}

