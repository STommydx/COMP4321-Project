package hk.ust.cse.comp4321.proj1;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The {@code CrawlUtils} class is an utility class
 * that contains utility methods for assisting the crawling procedure.
 */
public class CrawlUtils {

    private CrawlUtils() {
    }

    /**
     * Extracts words in the web page content.
     * <p>
     * note: uses {@link StringTokenizer} to tokenize the result
     *
     * @param doc the document to be extracted
     * @return a list of words in the web page body
     */
    public static Vector<String> extractWords(Document doc) {
        Vector<String> result = new Vector<>();
        String contents = doc.body().text();
        StringTokenizer st = new StringTokenizer(contents);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    /**
     * Extracts words from the title {@code String}
     *
     * @param title {String} the title
     * @return a vector of strings in title
     */
    public static Vector<String> extractTitleWords(String title) {
        Vector<String> result = new Vector<>();
        StringTokenizer st = new StringTokenizer(title);
        while (st.hasMoreTokens())
            result.add(st.nextToken());
        return result;
    }

    /**
     * Extracts useful external urls on the web page.
     * <p>
     * note: filter out images, emails, etc.
     *
     * @param doc the document to be extracted
     * @return a list of external links on the web page
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
     * Processes relative url to absolute url.
     * <p>
     * If it is absolute path, i.e., contains 'http' keyword, returns {@code url}.
     * {@code currentUrl} and {@code url} shall not be empty or null
     *
     * @param currentUrl the url of the page it is processing at
     * @param url        the url retrieved in the current url
     * @return processed URL
     */
    public static URL urlPreprocess(URL currentUrl, String url) throws MalformedURLException {
        return new URL(currentUrl, url);
    }

    /**
     * Returns {@code false} for unwanted junk links.
     * A filter for filtering out unwanted junk links.
     * <p>
     * The filter will remove all opaque links and all absolute links that is not in http or https scheme.
     *
     * @param linkString the link to be processed
     * @return {@code true} if the link is wanted
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

    /**
     * Removes the fragment part of an URL.
     *
     * @param linkString the url with fragment to be removed
     * @return the url with the fragment part removed
     */
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

}
