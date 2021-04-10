package hk.ust.cse.comp4321.proj1.nlp;

public class NLPUtils {
    private static final StopStem stopStem = new StopStem();

    private NLPUtils() {
    }

    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * @param word
     * @return
     */
    public static boolean stopwordFilter(String word) {
        if (word.startsWith("_")) return true;  // special tokens
        return !stopStem.isStopWord(word.toLowerCase());
    }

    /**
     * @param word
     * @return
     */
    public static String stemFilter(String word) {
        if (word.startsWith("_")) return word;  // special tokens
        return stopStem.stem(word);
    }
}