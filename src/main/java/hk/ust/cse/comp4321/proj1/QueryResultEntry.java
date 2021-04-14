package hk.ust.cse.comp4321.proj1;

import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryResultEntry {
    private final double similarity;
    private final DocumentRecord documentRecord;

    public QueryResultEntry(double similarity, DocumentRecord documentRecord) {
        this.similarity = similarity;
        this.documentRecord = documentRecord;
    }

    public double getSimilarity() {
        return similarity;
    }

    public DocumentRecord getDocumentRecord() {
        return documentRecord;
    }

    public static List<QueryResultEntry> loadQueryResult(List<Map.Entry<Integer, Double>> result, ForwardIndex forwardIndex, int maxCount) throws RocksDBException, IOException, ClassNotFoundException {
        List<QueryResultEntry> resultEntries = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : result) {
            if (resultEntries.size() > maxCount) break;
            DocumentRecord documentRecord = forwardIndex.get(entry.getKey());
            if (documentRecord != null)
                resultEntries.add(new QueryResultEntry(entry.getValue(), documentRecord));
        }
        return resultEntries;
    }
}
