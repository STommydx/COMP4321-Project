package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.InvertedIndex
import org.rocksdb.RocksDBException
import java.io.IOException

class MultiWordQuery
/**
 * Constructs a query using multiple query words.
 *
 * @param queryList List of words in query
 */(private val queryList: List<WordQuery>) : WordQuery() {
    /**
     * {@inheritDoc}
     */
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    override fun getRootSet(invertedIndex: InvertedIndex): Set<Int> {
        return queryList.flatMap { it.getRootSet(invertedIndex) }.toSet()
    }

    /**
     * {@inheritDoc}
     */
    override val queryVector: Map<String, Double>
        get() = // simple vector addition
            queryList
                    .map { it.queryVector }
                    .reduceOrNull { acc, map -> DocVectorUtils.add(acc, map) }
                    ?: HashMap()

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return queryList.toString()
    }
}
