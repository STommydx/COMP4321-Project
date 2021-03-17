package hk.ust.cse.comp4321.proj1;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.StringTokenizer;
import java.util.Vector;

public class CrawlUtils {

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
        // ADD YOUR CODES HERE
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String linkString = link.attr("href");
            // filter out false link
            if (!filterUrl(linkString)) {
//				System.out.printf("linkString: %s\n", linkString);
                continue;
            }
//			System.out.printf("no linkString: %s\n", linkString);
            result.add(linkString);
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
    public static String urlPreprocess(String currentUrl, String url) {
        if (url.contains("http"))
            return url;
        else {
            if (currentUrl.endsWith("/") && url.startsWith("/"))
                currentUrl = currentUrl.substring(0, currentUrl.length() - 1);
            return currentUrl + url;
        }
    }

    /**
     * To filter out unwanted junk links
     * return false if the link is unwanted
     *
     * @param linkString the link to be processed
     * @return if the link is wanted
     */
    public static boolean filterUrl(String linkString) {
        if (linkString.trim().isEmpty()) {
            return false;
        } else if (linkString.contains("mailto:")) {
            return false;
        } else if (linkString.contains("javascript")) {
            return false;
        } else if (linkString.charAt(0) == '#') {
            return false;
        }
        // cannot drop non cse site here
//		else if (!linkString.contains("cse.ust.hk/")){
//			return false;
//		}

        return true;
    }
}
