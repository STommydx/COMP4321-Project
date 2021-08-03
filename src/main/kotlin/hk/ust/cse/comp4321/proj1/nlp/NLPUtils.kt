package hk.ust.cse.comp4321.proj1.nlp

import java.util.*

/**
 * The `NLPUtils` class is an utility class
 * that contains utility methods for assisting the stemming and stop words removal procedure.
 */
object NLPUtils {
    private val stopStem = StopStem()

    /**
     * Checks if the string is non-null and only compose of alphabets and numbers
     * @param s input string
     * @return `true` if the string is non-null and only compose of alphabets or numbers followed by non-white-space characters
     */
    fun isAlphaNumeric(s: String): Boolean {
        return s.matches(Regex("^[a-zA-Z0-9]+$"))
    }

    /**
     * Checks if the keyword is a stop word by utilizing method defined at StopStem.java
     * @param word keyword
     * @return `true` if the word is not a stop word
     */
    @JvmStatic
    fun stopwordFilter(word: String): Boolean {
        return if (word.startsWith("_")) true else !stopStem.isStopWord(word.lowercase(Locale.getDefault())) // special tokens
    }

    /**
     * Returns the stemmed word for an input by utilizing method defined at StopStem.java
     *
     * @param word keyword
     * @return stemmed word
     */
    @JvmStatic
    fun stemFilter(word: String): String {
        if (word.startsWith("_")) return word // special tokens
        return if (word.contains(" ")) {
            word.split(" ").joinToString(" ") { stemFilter(it) }
        } else stopStem.stem(word)
    }
}
