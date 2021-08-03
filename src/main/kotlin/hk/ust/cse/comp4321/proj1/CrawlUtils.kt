package hk.ust.cse.comp4321.proj1

import org.jsoup.nodes.Document
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

/**
 * The `CrawlUtils` class is an utility class
 * that contains utility methods for assisting the crawling procedure.
 */
object CrawlUtils {
    /**
     * Extracts words in the web page content.
     *
     *
     * note: uses [StringTokenizer] to tokenize the result
     *
     * @param doc the document to be extracted
     * @return a list of words in the web page body
     */
    fun extractWords(doc: Document): List<String> {
        val result = ArrayList<String>()
        val contents = doc.body().text()
        val st = StringTokenizer(contents)
        while (st.hasMoreTokens()) {
            result.add(st.nextToken())
        }
        return result
    }

    /**
     * Extracts words from the title `String`
     *
     * @param title {String} the title
     * @return a vector of strings in title
     */
    fun extractTitleWords(title: String?): Vector<String> {
        val result = Vector<String>()
        val st = StringTokenizer(title)
        while (st.hasMoreTokens()) result.add(st.nextToken())
        return result
    }

    /**
     * Extracts useful external urls on the web page.
     *
     *
     * note: filter out images, emails, etc.
     *
     * @param doc the document to be extracted
     * @return a list of external links on the web page
     */
    fun extractLinks(doc: Document): Vector<String> {
        val result = Vector<String>()
        val links = doc.select("a[href]")
        for (link in links) {
            val linkString = link.attr("href")
            // filter out false link
            if (!filterUrl(linkString)) {
                continue
            }
            // remove URI fragment (#fragment)
            result.add(removeURIFragment(linkString))
        }
        return result
    }

    /**
     * Processes relative url to absolute url.
     *
     *
     * If it is absolute path, i.e., contains 'http' keyword, returns `url`.
     * `currentUrl` and `url` shall not be empty or null
     *
     * @param currentUrl the url of the page it is processing at
     * @param url        the url retrieved in the current url
     * @return processed URL
     */
    @Throws(MalformedURLException::class)
    fun urlPreprocess(currentUrl: URL?, url: String?): URL {
        return URL(currentUrl, url)
    }

    /**
     * Returns `false` for unwanted junk links.
     * A filter for filtering out unwanted junk links.
     *
     *
     * The filter will remove all opaque links and all absolute links that is not in http or https scheme.
     *
     * @param linkString the link to be processed
     * @return `true` if the link is wanted
     */
    fun filterUrl(linkString: String): Boolean {
        return try {
            val uri = URI(linkString)
            if (uri.isOpaque) false else !uri.isAbsolute || uri.scheme == "http" || uri.scheme == "https" // filter out mailto: or javascript:
        } catch (e: URISyntaxException) {
            false
        }
    }

    /**
     * Removes the fragment part of an URL.
     *
     * @param linkString the url with fragment to be removed
     * @return the url with the fragment part removed
     */
    fun removeURIFragment(linkString: String): String {
        return try {
            val uri = URI(linkString)
            val uriWithoutFragment = URI(uri.scheme, uri.rawSchemeSpecificPart, null)
            uriWithoutFragment.toString()
        } catch (e: URISyntaxException) {
            // unexpected error
            e.printStackTrace()
            linkString
        }
    }
}
