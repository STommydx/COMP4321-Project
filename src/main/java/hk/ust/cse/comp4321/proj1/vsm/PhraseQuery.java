package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.ForwardIndex;
import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PhraseQuery extends WordQuery{

    private String phrase;

    private List<String> listOfWords;

    public PhraseQuery(String phrase) {
        this.phrase = phrase;
        this.listOfWords = Arrays.asList(phrase.split(" "));
    }

    /**
     * Return a set with ID of documents which contains the phrase consecutively
     * @param invertedIndex to get the document ID from a word
     * @return Set of docID
     * @throws RocksDBException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> intersectionSet = null;
        // take intersection of all sets containing docID
        for (int i=0; i< listOfWords.size(); ++i){
            if (i==0) // set it for the first one
                intersectionSet = invertedIndex.getDocumentsFromWord(listOfWords.get(i));
            else //retainAll is "effectively an implementation for set intersection"
                intersectionSet.retainAll(invertedIndex.getDocumentsFromWord(listOfWords.get(i)));
        }

        if (intersectionSet == null) return null;

        Set<Integer> docIDResultSet = new HashSet<>();
        for (Integer docID : intersectionSet) {
            Set<Integer> resultSet = new HashSet<>();
            for (String listOfWord : listOfWords) {
                Set<Integer> locSet = invertedIndex.getWordPosFromDocuments(listOfWord, docID);
                if (resultSet.isEmpty()) {
                    resultSet.addAll(locSet);
                } else {
                    resultSet = filterConsecutiveOnly(resultSet, locSet);
                }
            }
            if (!resultSet.isEmpty())
                docIDResultSet.add(docID);
        }
        return docIDResultSet;
    }

    @Override
    public Map<String, Double> getQueryVector() {
        Map<String, Double> queryVector = new HashMap<>();
        for (String word : listOfWords)
            queryVector.put(word, 1.0);
        return queryVector;
    }


    private Set<Integer> filterConsecutiveOnly(Set<Integer> inputSet, Set<Integer> locSet){
        return inputSet
                .stream()
                .map(x->x+1)
                .filter(locSet::contains)
                .collect(Collectors.toSet());
    }
}
