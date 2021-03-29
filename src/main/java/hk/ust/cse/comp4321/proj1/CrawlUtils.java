package hk.ust.cse.comp4321.proj1;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

public class CrawlUtils {

    private static final StopStem stopStem = new StopStem();

    private CrawlUtils() {
    }

    /**
     * Extract words in the web page content.
     * note: use StringTokenizer to tokenize the result
     *
     * @param {Document} doc
     * @return {Vector<String>} a list of words in the web page body
     */
    public static Vector<String> extractWords(Document doc) {
        Vector<String> result = new Vector<>();
        // ADD YOUR CODES HERE
        String contents = doc.body().text();
        StringTokenizer st = new StringTokenizer(contents);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    /**
     * Extract useful external urls on the web page.
     * note: filter out images, emails, etc.
     *
     * @param {Document} doc
     * @return {Vector<String>} a list of external links on the web page
     */
    public static Vector<String> extractLinks(Document doc) {
        Vector<String> result = new Vector<>();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String linkString = link.attr("href");
            // filter out false link
            if (!filterUrl(linkString)) {
                continue;
            }
            // remove URI fragment (#fragment)
            result.add(removeURIFragment(linkString));
        }
        return result;
    }

    /**
     * To process relative url to absolute url.
     * If it is absolute path, i.e., contains 'http' keyword, returns url.
     * currentUrl and url shall not be empty or null
     *
     * @param currentUrl the url of the page it is processing at
     * @param url        the url retrieved in the current url
     * @return processed URL
     */
    public static URL urlPreprocess(URL currentUrl, String url) throws MalformedURLException {
        return new URL(currentUrl, url);
    }

    /**
     * To filter out unwanted junk links
     * return false if the link is unwanted
     *
     * @param linkString the link to be processed
     * @return if the link is wanted
     */
    public static boolean filterUrl(String linkString) {
        try {
            URI uri = new URI(linkString);
            if (uri.isOpaque()) return false; // filter out mailto: or javascript:
            return !uri.isAbsolute() || (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String removeURIFragment(String linkString) {
        try {
            URI uri = new URI(linkString);
            URI uriWithoutFragment = new URI(uri.getScheme(), uri.getRawSchemeSpecificPart(), null);
            return uriWithoutFragment.toString();
        } catch (URISyntaxException e) {
            // unexpected error
            e.printStackTrace();
            return linkString;
        }
    }

    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * @param word
     * @return
     */
    public static boolean stopwordFilter(String word) {
        return !stopStem.isStopWord(word);
    }

    /**
     * @param word
     * @return
     */
    public static String stemFilter(String word) {
        return stopStem.stem(word);
    }
}
