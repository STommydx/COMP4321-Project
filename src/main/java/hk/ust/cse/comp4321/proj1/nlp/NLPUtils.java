package hk.ust.cse.comp4321.proj1.nlp;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The {@code NLPUtils} class is an utility class
 * that contains utility methods for assisting the stemming and stop words removal procedure.
 */
public class NLPUtils {
    private static final StopStem stopStem = new StopStem();

    private NLPUtils() {
    }

    /**
     * Checks if the string is non-null and only compose of alphabets and numbers
     * @param s input string
     * @return {@code true} if the string is non-null and only compose of alphabets or numbers followed by non-white-space characters
     */
    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Checks if the keyword is a stop word by utilizing method defined at StopStem.java
     * @param word keyword
     * @return {@code true} if the word is not a stop word
     */
    public static boolean stopwordFilter(String word) {
        if (word.startsWith("_")) return true;  // special tokens
        return !stopStem.isStopWord(word.toLowerCase());
    }

    /**
     * Returns the stemmed word for an input by utilizing method defined at StopStem.java
     *
     * @param word keyword
     * @return stemmed word
     */
    public static String stemFilter(String word) {
        if (word.startsWith("_")) return word;  // special tokens
        if (word.contains(" ")){
            return Arrays.stream(word.split(" "))
                    .map(NLPUtils::stemFilter)
                    .collect(Collectors.joining(" "));
        }
        return stopStem.stem(word);
    }
}
