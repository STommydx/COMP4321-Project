package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;

public class PhraseQuery extends WordQuery {

    private final String phrase;

    private final List<String> listOfWords;

    public PhraseQuery(String phrase) {
        this.phrase = phrase;
        this.listOfWords = Arrays.asList(phrase.split(" "));
    }

    /**
     * Return a set with ID of documents which contains the phrase consecutively
     *
     * @param invertedIndex to get the document ID from a word
     * @return Set of docID, empty set if the phrase does not exist in any doc
     * @throws RocksDBException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> intersectionSet = null;
        // take intersection of all sets containing docID
        for (int i = 0; i < listOfWords.size(); ++i) {
            if (i == 0) // set it for the first one
                intersectionSet = invertedIndex.getDocumentsFromWord(listOfWords.get(i));
            else //retainAll is "effectively an implementation for set intersection"
                intersectionSet.retainAll(invertedIndex.getDocumentsFromWord(listOfWords.get(i)));
        }

        if (intersectionSet == null) intersectionSet = new HashSet<>();

        Map<Integer, ArrayList<Integer>> oldPosting = null;
        for (String word : listOfWords) {
            Map<Integer, ArrayList<Integer>> postingList = invertedIndex.get(word);

            // if one of the words is not in invertedIndex
            if (postingList == null)
                return new HashSet<>();

            // run this if it is the 0th iteration
            if (oldPosting == null) {
                oldPosting = postingList;
                continue;
            }

            Set<Integer> newIntersectionSet = new HashSet<>();
            Map<Integer, ArrayList<Integer>> nextPostingList = new HashMap<>();
            for (Integer docID : intersectionSet) {
                ArrayList<Integer> oldPosList = oldPosting.get(docID);
                ArrayList<Integer> posList = postingList.get(docID);
                ArrayList<Integer> nextPosList = new ArrayList<>();

                for (Integer pos : oldPosList) {
                    if (pos >= 0) {
                        // last word is in body, and grow in +ve direction
                        if (posList.contains(pos + 1)) {
                            nextPosList.add(pos + 1);
                        }
                    } else {
                        // the last word is in title, and grow in -ve direction
                        if (posList.contains(pos - 1)) {
                            nextPosList.add(pos - 1);
                        }
                    }
                }
                if (!nextPosList.isEmpty()) {
                    newIntersectionSet.add(docID); // this docID contains the sequence
                    nextPostingList.put(docID, nextPosList);
                }
            }

            // Update set and map for next (word) iteration
            intersectionSet = newIntersectionSet;
            oldPosting = nextPostingList;
        }

        return intersectionSet;
    }

    @Override
    public Map<String, Double> getQueryVector() {
        Map<String, Double> queryVector = new HashMap<>();
        for (String word : listOfWords)
            queryVector.put(word, 1.0);
        return queryVector;
    }

    @Override
    public String toString() {
        return "\"" + phrase + "\"";
    }
}
