package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.ForwardIndex
import hk.ust.cse.comp4321.proj1.InvertedIndex
import org.rocksdb.RocksDBException
import java.io.IOException

abstract class Query {
    /**
     * Return a set consisting of ID of documents which contain the interested term(s)
     *
     * @param invertedIndex to get the document ID from a word
     * @return set of docID
     * @throws RocksDBException       Rocksdb error
     * @throws IOException            Failed to perform IO operation
     * @throws ClassNotFoundException Class not found
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    abstract fun getRootSet(invertedIndex: InvertedIndex): Set<Int>

    /**
     * Return a map between document ID and similarity score
     * @param forwardIndex to get the list of keywords and respective frequency for calculation
     * @param invertedIndex to get the document ID from a word
     * @param docs to know documents of what docID will be involved in the calculation
     * @return a map between document ID and similarity score
     * @throws RocksDBException Rocksdb error
     * @throws IOException Failed to perform IO operation
     * @throws ClassNotFoundException Class not found
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    abstract fun getSimilarityScore(forwardIndex: ForwardIndex, invertedIndex: InvertedIndex, docs: Set<Int>): Map<Int, Double>

    /**
     * Return a list of maps between document ID and similarity score that is related to the input query
     * @param forwardIndex to get the list of keywords and respective frequency for calculation
     * @param invertedIndex to get the document ID from a word
     * @return a list of maps between document ID and similarity score
     * @throws RocksDBException Rocksdb error
     * @throws IOException Failed to perform IO operation
     * @throws ClassNotFoundException Class not found
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    fun query(forwardIndex: ForwardIndex, invertedIndex: InvertedIndex): List<Map.Entry<Int, Double>> {
        val rootSet = getRootSet(invertedIndex)
        val similarityScore = getSimilarityScore(forwardIndex, invertedIndex, rootSet)
        return similarityScore.entries
                .sortedByDescending { it.value }
    }

    companion object {
        fun parse(query: String): Query {
            val tokenizeQuery = QueryUtils.tokenizeQuery(query)
            val preprocessedQuery = QueryUtils.preprocessQuery(tokenizeQuery)
            return parse(preprocessedQuery)
        }

        fun parse(query: List<String>): Query {
            // parse OR operator
            val orSubRawQueries = QueryUtils.querySplit(query, QueryUtils.OR_TOKEN)
            if (orSubRawQueries.size >= 2) {
                val orSubQueries = orSubRawQueries.map { parse(it) }
                return BooleanQuery(orSubQueries, BooleanQuery.Operator.OR)
            }

            // parse AND operator
            val andSubRawQueries = QueryUtils.querySplit(query, QueryUtils.AND_TOKEN)
            if (andSubRawQueries.size >= 2) {
                val andSubQueries = andSubRawQueries.map { parse(it) }
                return BooleanQuery(andSubQueries, BooleanQuery.Operator.AND)
            }
            val wordQueryList: MutableList<WordQuery> = ArrayList()
            for (queryWord in query) {
                if (queryWord.contains(" ")) wordQueryList.add(PhraseQuery(queryWord)) else wordQueryList.add(SimpleWordQuery(queryWord))
            }
            return MultiWordQuery(wordQueryList)
        }
    }
}
