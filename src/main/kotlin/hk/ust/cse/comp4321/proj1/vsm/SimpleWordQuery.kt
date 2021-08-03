package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.InvertedIndex
import org.rocksdb.RocksDBException
import java.io.IOException

/**
 * The `SimpleWordQuery` class represent queries with a single keyword.
 *
 *
 * The vector would be a unit vector pointing at the direction of the keyword
 * in the vector space.
 *
 * @see Query
 *
 * @see WordQuery
 *
 * @see MultiWordQuery
 */
class SimpleWordQuery
/**
 * Constructs a query using a single query word.
 *
 * @param word the query keyword
 */(private val word: String) : WordQuery() {

    /**
     * {@inheritDoc}
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    override fun getRootSet(invertedIndex: InvertedIndex): Set<Int> {
        return invertedIndex.getDocumentsFromWord(word)
    }

    /**
     * {@inheritDoc}
     */
    override val queryVector: Map<String, Double>
        get() {
            val queryVector: MutableMap<String, Double> = HashMap()
            queryVector[word] = 1.0
            return queryVector
        }

    /**
     * Gets the string representation of the query.
     *
     *
     * The method simply returns the query keyword.
     *
     * @return the string representation of the query
     */
    override fun toString(): String {
        return word
    }
}
