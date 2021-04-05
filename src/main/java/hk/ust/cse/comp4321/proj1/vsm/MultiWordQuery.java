package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;

public class MultiWordQuery extends WordQuery {

    private final List<WordQuery> queryList;

    public MultiWordQuery(List<WordQuery> queryList) {
        this.queryList = queryList;
    }

    @Override
    public Set<Integer> getRootSet(InvertedIndex invertedIndex) throws RocksDBException, IOException, ClassNotFoundException {
        Set<Integer> set = new HashSet<>();
        for (WordQuery wordQuery : queryList) {
            Set<Integer> rootSet = wordQuery.getRootSet(invertedIndex);
            set.addAll(rootSet);
        }
        return set;
    }

    @Override
    public Map<String, Double> getQueryVector() {
        HashMap<String, Double> queryVector = new HashMap<>();
        // simple vector addition
        queryList.stream()
                .map(WordQuery::getQueryVector)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .forEach(mapEntry -> queryVector.put(mapEntry.getKey(),
                        queryVector.getOrDefault(mapEntry.getKey(), 0.) + mapEntry.getValue()));
        return queryVector;
    }

    @Override
    public Map<Integer, Map<String, Double>> getDocumentVectors(InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Map<String, Double>> docVectors = new HashMap<>();
        List<Map<Integer, Map<String, Double>>> docVectorsList = new ArrayList<>();
        for (WordQuery q : queryList) {
            Map<Integer, Map<String, Double>> documentVectors = q.getDocumentVectors(invertedIndex, docs);
            docVectorsList.add(documentVectors);
        }
        for (int doc : docs) {
            HashMap<String, Double> docSubVector = new HashMap<>();
            // simple vector addition
            docVectorsList.stream()
                    .map(vec -> vec.get(doc))
                    .map(Map::entrySet)
                    .flatMap(Set::stream)
                    .forEach(mapEntry -> docSubVector.put(mapEntry.getKey(),
                            docSubVector.getOrDefault(mapEntry.getKey(), 0.) + mapEntry.getValue()));
            docVectors.put(doc, docSubVector);
        }
        return docVectors;
    }

    @Override
    public String toString() {
        return queryList.toString();
    }

}
