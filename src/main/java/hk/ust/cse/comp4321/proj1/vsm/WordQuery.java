package hk.ust.cse.comp4321.proj1.vsm;

import hk.ust.cse.comp4321.proj1.ForwardIndex;
import hk.ust.cse.comp4321.proj1.InvertedIndex;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code WordQuery} class represents queries under the vector space model.
 * <p>
 * It is an abstract class which allows vector space model operations such as
 * getting the representation of documents in vector space and calculating
 * similarity scores for the query.
 *
 * @see Query
 * @see SimpleWordQuery
 * @see MultiWordQuery
 */
public abstract class WordQuery extends Query {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Double> getSimilarityScore(ForwardIndex forwardIndex, InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Map<Integer, Map<String, Double>> documentVectors = getDocumentVectors(forwardIndex, invertedIndex, docs);
        Map<String, Double> queryVector = getQueryVector();
        Map<Integer, Double> similarityScores = new HashMap<>();
        for (int doc : docs) {
            Map<String, Double> v1 = documentVectors.get(doc);
            if (v1 != null)
                similarityScores.put(doc, QueryUtils.cosineSimilarity(v1, queryVector));
        }
        return similarityScores;
    }

    /**
     * Gets the query vector of the query.
     *
     * @return the query vector
     */
    public abstract Map<String, Double> getQueryVector();

    /**
     * Retrieves the document vectors for a set of documents.
     *
     * @param forwardIndex  the forward index RocksDB
     * @param invertedIndex the inverted index RocksDB
     * @param docs          the set of documents to retrieve document vectors
     * @return a map that maps the document index to the document vectors
     * @throws RocksDBException       if there is an error from RocksDB
     * @throws IOException            if there is an I/O problem
     * @throws ClassNotFoundException if the deserialization fails
     */
    public static Map<Integer, Map<String, Double>> getDocumentVectors(ForwardIndex forwardIndex, InvertedIndex invertedIndex, Set<Integer> docs) throws RocksDBException, IOException, ClassNotFoundException {
        Set<String> terms = new HashSet<>();
        Map<Integer, Map<String, Double>> termFrequencies = new HashMap<>();
        for (int doc : docs) {
            Map<String, Double> termFrequency = forwardIndex.getNormalizedTfVector(doc);
            terms.addAll(termFrequency.keySet());
            termFrequencies.put(doc, termFrequency);
        }

        Map<String, Double> invertedDocumentFrequencies = new HashMap<>();
        for (String term : terms) {
            invertedDocumentFrequencies.put(term, invertedIndex.getIdf(term));
        }

        return termFrequencies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        x -> DocVectorUtils.elementwiseProduct(x.getValue(), invertedDocumentFrequencies)
                ));
    }

}
