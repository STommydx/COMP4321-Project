package hk.ust.cse.comp4321.proj1.nlp

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * The `StopStem` class do stemming and stop words removal by referencing to Porter.java and "stopwords-en.txt" respectively
 */
class StopStem {
    private val porter: Porter = Porter()
    private val stopWords: HashSet<String> = HashSet()

    /**
     * Checks if the string is a stop word
     * @param str input string
     * @return `true` if the string is a stop word
     */
    fun isStopWord(str: String): Boolean {
        return stopWords.contains(str)
    }

    /**
     * Returns the stemmed string of an input by utilizing Porter.java
     * @param str input string
     * @return the stemmed string
     */
    fun stem(str: String?): String {
        return porter.stripAffixes(str)
    }

    companion object {
        private const val STOPWORD_RESOURCE = "stopwords-en.txt"
    }

    /**
     * Load all stop words from "stopwords-en.txt" and report number of stop words loaded
     * Report warning if failed to open resource file
     */
    init {
        try {
            val `is` = javaClass.classLoader.getResourceAsStream(STOPWORD_RESOURCE)
            if (`is` != null) {
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    var line: String
                    while (reader.readLine().also { line = it } != null) {
                        stopWords.add(line)
                    }
                }
                println("Total of " + stopWords.size + " stopwords loaded.")
            } else {
                System.err.println("Warning: Fail to open resource file: $STOPWORD_RESOURCE")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
