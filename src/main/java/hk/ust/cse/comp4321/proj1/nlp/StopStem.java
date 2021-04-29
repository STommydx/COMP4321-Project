package hk.ust.cse.comp4321.proj1.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * The {@code StopStem} class do stemming and stop words removal by referencing to Porter.java and "stopwords-en.txt" respectively
 */
public class StopStem {
    private static final String STOPWORD_RESOURCE = "stopwords-en.txt";

    private final Porter porter;
    private final HashSet<String> stopWords;

    /**
     * Load all stop words from "stopwords-en.txt" and report number of stop words loaded
     * Report warning if failed to open resource file
     */
    public StopStem() {
        porter = new Porter();
        stopWords = new HashSet<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(STOPWORD_RESOURCE);
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stopWords.add(line);
                    }
                }
                System.out.println("Total of " + stopWords.size() + " stopwords loaded.");
            } else {
                System.err.println("Warning: Fail to open resource file: " + STOPWORD_RESOURCE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the string is a stop word
     * @param str input string
     * @return {@code true} if the string is a stop word
     */
    public boolean isStopWord(String str) {
        return stopWords.contains(str);
    }

    /**
     * Returns the stemmed string of an input by utilizing Porter.java
     * @param str input string
     * @return the stemmed string
     */
    public String stem(String str) {
        return porter.stripAffixes(str);
    }
}