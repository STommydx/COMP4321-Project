package hk.ust.cse.comp4321.proj1;

import java.io.*;
import java.util.HashSet;

public class StopStem {
    private static final String STOPWORD_RESOURCE = "stopwords-en.txt";

    private final Porter porter;
    private final HashSet<String> stopWords;

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

    public boolean isStopWord(String str) {
        return stopWords.contains(str);
    }

    public String stem(String str) {
        return porter.stripAffixes(str);
    }
}