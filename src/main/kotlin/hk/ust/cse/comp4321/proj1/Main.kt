package hk.ust.cse.comp4321.proj1

import com.fasterxml.jackson.databind.ObjectMapper
import hk.ust.cse.comp4321.proj1.rocks.RocksAbstractMap
import hk.ust.cse.comp4321.proj1.vsm.Query
import org.rocksdb.RocksDBException
import picocli.CommandLine
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.stream.Collectors

object Main {

    private val mapper = ObjectMapper()
    private val forwardDbName = System.getenv("SE_DB_FORWARD_INDEX") ?: "ForwardIndex"
    private val invertedDbName = System.getenv("SE_DB_INVERTED_INDEX") ?: "InvertedIndex"
    private val lookupDbName = System.getenv("SE_DB_LOOKUP_TABLE") ?: "LookupTable"
    private val suggestDbName = System.getenv("SE_DB_SUGGEST_INDEX") ?: "SuggestIndex"

    fun crawl(crawlUrl: URL, forwardDb: String?, invertedDb: String?, lookupDb: String?, suggestDb: String?, crawlPages: Int, crawlDepth: Int) {
        val crawler = Crawler(crawlUrl, crawlPages, crawlDepth)
        println("Crawler now crawling from root URL $crawlUrl...")
        crawler.crawlLoop()

        // put in databases
        val documentRecordList = crawler.documentRecords
        println("Crawler finished crawling. Total of " + documentRecordList.size + " documents are retrieved.")
        println("Inserting crawled records to RocksDB tables...")
        try {
            val forwardDatabase = ForwardIndex.getInstance(forwardDb!!)
            val invertedDatabase = InvertedIndex.getInstance(invertedDb!!)
            val urlDatabase = DocumentLookupIndex.getInstance(lookupDb)
            var recordAdded = 0
            var recordModified = 0

            // store inverted index updates in memory first
            val invertedIndexUpdates: MutableMap<String, TreeMap<Int, ArrayList<Int>>> = HashMap()
            println("Putting parent links into document records...")
            val memoryLookUpTable = documentRecordList.stream().collect(Collectors.toMap({ obj: DocumentRecord -> obj.url }, { x: DocumentRecord? -> x }))
            for (documentRecord in documentRecordList) {
                val childLinks: List<URL> = documentRecord.childLinks
                for (link in childLinks) {
                    val dr = memoryLookUpTable[link] ?: continue
                    dr.addParentLinks(documentRecord.url)
                }
            }
            println("Parent links put into document records successfully")
            for (documentRecord in documentRecordList) {
                val urlKey = urlDatabase[documentRecord.url.toString()]
                var currentKey: Int
                if (urlKey == null) {  // if url not exist in database
                    // get a new key to for this url/documentRecord and update DB
                    currentKey = forwardDatabase.andIncrementNextID
                    urlDatabase.put(documentRecord.url.toString(), currentKey)
                    recordAdded++
                } else {
                    currentKey = urlKey
                    val dbRecord = forwardDatabase[currentKey]
                    if (dbRecord != null && dbRecord.lastModificationDate == documentRecord.lastModificationDate) {
                        // same modification date, do nothing
                        continue
                    }
                    recordModified++
                }

                // update forward index
                forwardDatabase.put(currentKey, documentRecord)

                // batch update inverted index with word location
                val words = documentRecord.wordsWithLoc
                for ((keyword, value) in words) {
                    val data = invertedIndexUpdates.getOrDefault(keyword, TreeMap())
                    data[currentKey] = value
                    invertedIndexUpdates[keyword] = data
                }
            }
            println("Successfully inserted all records into Forward Index.")

            // update inverted index
            var invertedAdded = 0
            var invertedModified = 0
            for ((key, value) in invertedIndexUpdates) {
                val data = invertedDatabase[key]
                if (data != null) {
                    data.putAll(value)
                    invertedDatabase.put(key, data)
                    invertedModified++
                } else {
                    invertedDatabase.put(key, value)
                    invertedAdded++
                }
            }
            println("Successfully inserted all records into RocksDB.")
            println("Forward Index: $recordAdded added. $recordModified modified.")
            println("Inverted Index: $invertedAdded added. $invertedModified modified.")
            if (suggestDb != null) {
                println("Building suggestion index...")
                val suggestionTrie = SuggestionTrie()
                for ((key, value) in invertedIndexUpdates) {
                    suggestionTrie.put(key, value.size)
                }
                suggestionTrie.buildSuggestion(20)
                val res = suggestionTrie.all
                val suggestionIndex = SuggestionIndex.getInstance(suggestDb)
                for ((k, v) in res) {
                    suggestionIndex.put(k, ArrayList(v.suggestions.keys))
                }
                println("Successfully built suggestion index.")
            }
        } catch (e: RocksDBException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun printRecords(file: File, forwardDb: String) {
        try {
            PrintWriter(file).use { writer ->
                val db = ForwardIndex.getInstance(forwardDb)
                println("Successfully opened Forward Index table $forwardDb. Reading records...")
                val it: RocksAbstractMap<Int, DocumentRecord>.Iterator = db.iterator()
                while (it.isValid) {
                    writer.println("----------------------")
                    writer.println(it.value())
                    it.next()
                }
                println("All records printed in $file.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: RocksDBException) {
            e.printStackTrace()
        }
    }

    fun queryAndPrint(queryString: String, forwardDb: String, invertedDb: String) {
        println("Now querying: '$queryString'")
        val query = Query.parse(queryString)
        println("Parsed query: '$query'")
        try {
            val forwardIndex = ForwardIndex.getInstance(forwardDb)
            val invertedIndex = InvertedIndex.getInstance(invertedDb)
            invertedIndex.numOfDocuments = forwardIndex.nextID // hacky way to get approx. no of documents
            val rawResult = query.query(forwardIndex, invertedIndex)
            println("Raw query result: $rawResult")
            val result = QueryResultEntry.loadQueryResult(rawResult, forwardIndex, 50)
            println("Result: " + mapper.writeValueAsString(result))
        } catch (e: RocksDBException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    @JvmOverloads
    @JvmStatic
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    fun queryRaw(queryString: String, forwardDb: String = forwardDbName, invertedDb: String = invertedDbName): List<QueryResultEntry> {
        val query = Query.parse(queryString)
        val forwardIndex = ForwardIndex.getInstance(forwardDb)
        val invertedIndex = InvertedIndex.getInstance(invertedDb)
        invertedIndex.numOfDocuments = forwardIndex.nextID // hacky way to get approx. no of documents
        val rawResult = query.query(forwardIndex, invertedIndex)
        return QueryResultEntry.loadQueryResult(rawResult, forwardIndex, 50)
    }

    @JvmOverloads
    @JvmStatic
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    fun query(queryString: String, forwardDb: String = forwardDbName, invertedDb: String = invertedDbName): String {
        val resultEntries = queryRaw(queryString, forwardDb, invertedDb)
        return mapper.writeValueAsString(resultEntries)
    }

    @JvmOverloads
    @JvmStatic
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    fun suggest(queryString: String, suggestDb: String = suggestDbName): String {
        val spaceIdx = queryString.lastIndexOf(" ")
        val suggestionIndex = SuggestionIndex.getInstance(suggestDb)
        return if (spaceIdx == -1) {
            val suggestions: List<String?> = suggestionIndex[queryString] ?: ArrayList()
            mapper.writeValueAsString(suggestions)
        } else {
            val prefix = queryString.substring(0, spaceIdx + 1)
            val suffix = queryString.substring(spaceIdx + 1)
            var suggestions: List<String> = suggestionIndex[suffix] ?: ArrayList()
            suggestions = suggestions.stream().map { x: String -> prefix + x }.collect(Collectors.toList())
            mapper.writeValueAsString(suggestions)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val commandOptions = CommandOptions()
        val commandLine = CommandLine(commandOptions)
        commandLine.parseArgs(*args)
        if (commandLine.isUsageHelpRequested) {
            commandLine.usage(System.out)
            return
        }
        if (commandOptions.shouldCrawl) {
            try {
                val crawlURL = URL(commandOptions.crawlUrl)
                crawl(crawlURL, commandOptions.forwardDb, commandOptions.invertedDb,
                        commandOptions.lookupDb, commandOptions.suggestDb, commandOptions.crawlPages,
                        commandOptions.crawlDepth)
            } catch (e: MalformedURLException) {
                println("The URL provided (" + commandOptions.crawlUrl + ") is not a valid URL.")
            }
        }
        if (commandOptions.shouldPrintRecords) {
            printRecords(commandOptions.outputFile, commandOptions.forwardDb)
        }
        if (commandOptions.queryString != null) {
            queryAndPrint(commandOptions.queryString!!, commandOptions.forwardDb, commandOptions.invertedDb)
        }
        if (commandOptions.interactiveQuery) {
            Scanner(System.`in`).use { scanner ->
                while (scanner.hasNextLine()) {
                    queryAndPrint(scanner.nextLine(), commandOptions.forwardDb, commandOptions.invertedDb)
                }
            }
        }
    }

    class CommandOptions {
        @CommandLine.Option(names = ["-c", "--crawl"], description = ["Crawl the web to update database document records"])
        var shouldCrawl = false

        @CommandLine.Option(names = ["-u", "--url"], description = ["The root URL to crawl"])
        var crawlUrl = "https://www.cse.ust.hk/"

        @CommandLine.Option(names = ["-d", "--depth"], description = ["The maximum recursive depth when crawling the pages"])
        var crawlDepth = 5

        @CommandLine.Option(names = ["-n", "--num-docs"], description = ["The maximum number of documents that should be crawled"])
        var crawlPages = 30

        @CommandLine.Option(names = ["-q", "--query"], description = ["The query term for query"])
        var queryString: String? = null

        @CommandLine.Option(names = ["-Q", "--query-interactive"], description = ["Interactive query mode: input query from standard input"])
        var interactiveQuery = false

        @CommandLine.Option(names = ["-f", "--forward-index"], description = ["The database name of the forward index"])
        var forwardDb = forwardDbName

        @CommandLine.Option(names = ["-i", "--inverted-index"], description = ["The database name of the inverted index"])
        var invertedDb = invertedDbName

        @CommandLine.Option(names = ["-l", "--lookup-table"], description = ["The database name of the url to id lookup table"])
        var lookupDb = lookupDbName

        @CommandLine.Option(names = ["-s", "--suggestion-index"], description = ["The database name of the suggestion index"])
        var suggestDb = suggestDbName

        @CommandLine.Option(names = ["-p", "--print"], description = ["Printing database forward index to file"])
        var shouldPrintRecords = false

        @CommandLine.Option(names = ["-o", "--output"], description = ["The file to print for printing database records"])
        var outputFile = File("spider_result.txt")

        @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Display this help message"])
        var usageHelpRequested = false
    }
}
