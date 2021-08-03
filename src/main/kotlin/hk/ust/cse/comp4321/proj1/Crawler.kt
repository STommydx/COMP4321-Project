package hk.ust.cse.comp4321.proj1

import hk.ust.cse.comp4321.proj1.nlp.NLPUtils
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL
import java.util.*
import javax.net.ssl.SSLHandshakeException


class Crawler constructor(private val rootURL: URL, private val maxPages: Int, private val maxDepth: Int) {
    /**
     * The data structure for the crawling queue.
     */
    class Link(var url: URL, var level: Int)

    /**
     * This is customized exception for those pages that have been visited before.
     */
    class RevisitException : RuntimeException()

    private val urls // the set of urls that have been visited before
            : HashSet<URL> = HashSet()
    var todos // the queue of URLs to be crawled
            : Vector<Link> = Vector()
    private var counter = 0 // to count the number of retrieved pages
    val documentRecords: MutableList<DocumentRecord> = ArrayList()

    /**
     * Send an HTTP request and analyze the response.
     *
     * @return {Response} res
     * @throws HttpStatusException for non-existing pages
     * @throws IOException
     */
    @Throws(HttpStatusException::class, IOException::class)
    fun getResponse(url: URL): Connection.Response {
        if (urls.contains(url)) {
            throw RevisitException() // if the page has been visited, break the function
        }

        // the default body size is 2Mb, to attain unlimited page, use the following.
        // Connection conn = Jsoup.connect(this.url).maxBodySize(0).followRedirects(false);
        val conn = Jsoup.connect(url.toString()).followRedirects(false)

        /* establish the connection and retrieve the response */
        val res = conn.execute()

        /* if the link redirects to other place... */if (res.hasHeader("location")) {
            val actualUrl = URL(url, res.header("location"))
            if (urls.contains(actualUrl)) {
                throw RevisitException()
            } else {
                urls.add(actualUrl)
            }
        } else {
            urls.add(url)
        }
        return res
    }

    /**
     * Use a queue to manage crawl tasks.
     */
    fun crawlLoop() {
        while (!todos.isEmpty()) {
            val focus = todos.removeAt(0)
            if (focus.level > maxDepth) break // stop criteria
            if (urls.contains(focus.url)) continue  // ignore pages that has been visited
            if (counter >= maxPages) {  // stop when number of pages checked out exceed the constant
                break
            } else {
                counter++
            }

            /* start to crawl on the page */try {
                val returns = getResponse(focus.url)
                val res = returns.bufferUp()
                val doc = res.parse()

                // Check lang
                val htmlLang = doc.select("html").first()!!.attr("lang")
                val bodyLang = doc.select("body").first()!!.attr("lang")
                val lang = htmlLang + bodyLang
                if (!lang.lowercase(Locale.getDefault()).contains("en")) {
                    System.out.printf("\n skipped link= %s\n", focus.url)
                    continue
                }
                // Check lang end
                val words = CrawlUtils.extractWords(doc)
                        .filter { NLPUtils.isAlphaNumeric(it) }
                        .filter { NLPUtils.stopwordFilter(it) }
                        .map { NLPUtils.stemFilter(it) }
                val extractedLinks = CrawlUtils.extractLinks(doc)
                val linkSet: MutableSet<URL> = HashSet()
                for (extractedLink in extractedLinks) {
                    val link = CrawlUtils.urlPreprocess(focus.url, extractedLink)
                    if (link.host == rootURL.host) {
                        todos.add(Link(link, focus.level + 1)) // add links
                        linkSet.add(link)
                    }
                }
                val linksList = ArrayList(linkSet)

                // retrieving data
                var lastModified = res.header("Last-Modified")
                if (lastModified == null || lastModified == "") {
                    lastModified = res.header("Date")
                }
                val size = res.bodyAsBytes().size

                // count keywords
                val freqTable = TreeMap<String, Int>()
                for (item in words) {
                    freqTable[item] = freqTable.getOrDefault(item, 0) + 1
                }

                // count title as keywords with higher weight,
                val title = res.parse().title()
                val titleFreqTable = TreeMap<String, Int>()
                val tokenizedTitle = CrawlUtils.extractTitleWords(title)
                        .filter { NLPUtils.isAlphaNumeric(it) }
                        .filter { NLPUtils.stopwordFilter(it) }
                        .map { NLPUtils.stemFilter(it) }
                for (item in tokenizedTitle) {
                    titleFreqTable[item] = titleFreqTable.getOrDefault(item, 0) + 1
                }

                // Calling document record to serialise the retrieved data
                val documentRecord = DocumentRecord(focus.url)
                documentRecord.setTitle(title)
                        .setLastModificationDate(Date(lastModified))
                        .setFreqTable(freqTable)
                        .setTitleFreqTable(titleFreqTable)
                        .setPageSize(size)
                        .setChildLinks(linksList)
                        .setWords(words)
                        .setTitleWords(tokenizedTitle)
                documentRecords.add(documentRecord)
            } catch (e: HttpStatusException) {
                // e.printStackTrace ();
                System.out.printf("\nLink Error: %s\n", focus.url)
            } catch (e: SSLHandshakeException) {
                System.out.printf("\nSSL Error: %s\n", focus.url)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (ignored: RevisitException) {
            } catch (e: Exception) {
                System.out.printf("\nUnhandled error: %s\n", e.message)
            }
        }
    }

    init {
        todos.add(Link(rootURL, 1))
    }
}
