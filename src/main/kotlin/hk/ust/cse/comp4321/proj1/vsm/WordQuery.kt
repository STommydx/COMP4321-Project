package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.ForwardIndex
import hk.ust.cse.comp4321.proj1.InvertedIndex
import org.rocksdb.RocksDBException
import java.io.IOException

/**
 * The `WordQuery` class represents queries under the vector space model.
 *
 *
 * It is an abstract class which allows vector space model operations such as
 * getting the representation of documents in vector space and calculating
 * similarity scores for the query.
 *
 * @see Query
 *
 * @see SimpleWordQuery
 *
 * @see MultiWordQuery
 */
abstract class WordQuery : Query() {
    /**
     * {@inheritDoc}
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    override fun getSimilarityScore(forwardIndex: ForwardIndex, invertedIndex: InvertedIndex, docs: Set<Int>): Map<Int, Double> {
        val documentVectors = getDocumentVectors(forwardIndex, invertedIndex, docs)
        val queryVector = queryVector
        return docs
                .filter { documentVectors.contains(it) }
                .associateWith { QueryUtils.cosineSimilarity(documentVectors[it]!!, queryVector) }
    }

    /**
     * Gets the query vector of the query.
     *
     * @return the query vector
     */
    abstract val queryVector: Map<String, Double>

    companion object {
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
        @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
        fun getDocumentVectors(forwardIndex: ForwardIndex, invertedIndex: InvertedIndex, docs: Set<Int>): Map<Int, Map<String, Double>> {
            val termFrequencies = docs.associateWith { forwardIndex.getNormalizedTfVector(it) }
            val terms = termFrequencies.values.flatMap { it.keys }.toSet()
            val invertedDocumentFrequencies = terms.associateWith { invertedIndex.getIdf(it) }
            return termFrequencies.mapValues { DocVectorUtils.elementwiseProduct(it.value, invertedDocumentFrequencies) }
        }
    }
}
