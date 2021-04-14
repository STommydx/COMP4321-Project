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
        // simple vector addition
        return queryList.stream()
                .map(WordQuery::getQueryVector)
                .reduce(DocVectorUtils::add)
                .orElse(new HashMap<>());
    }

    @Override
    public String toString() {
        return queryList.toString();
    }

}
