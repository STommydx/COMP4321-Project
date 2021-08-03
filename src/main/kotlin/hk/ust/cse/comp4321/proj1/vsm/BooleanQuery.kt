package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.ForwardIndex
import hk.ust.cse.comp4321.proj1.InvertedIndex
import org.rocksdb.RocksDBException
import java.io.IOException
import java.util.stream.Collectors
import kotlin.math.sqrt

/**
 * The `Query` class implements the extended boolean model by allowing specification of multiple queries connected
 * by boolean operators (AND, OR)
 */
class BooleanQuery
/**
 * Constructs a boolean query using a query with operator (AND, OR)
 *
 * @param queryList the query list
 * @param op the operator specified in the query
 */(private val queryList: List<Query>, private val op: Operator) : Query() {

    enum class Operator(private val operatorName: String) {
        AND("AND"), OR("OR");

        /**
         * {@inheritDoc}
         */
        override fun toString(): String {
            return operatorName
        }
    }

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
    @Throws(RocksDBException::class, IOException::class, ClassNotFoundException::class)
    override fun getSimilarityScore(forwardIndex: ForwardIndex, invertedIndex: InvertedIndex, docs: Set<Int>): Map<Int, Double> {
        val similarityScoreList: MutableList<Map<Int, Double>> = ArrayList()
        for (subquery in queryList) {
            similarityScoreList.add(subquery.getSimilarityScore(forwardIndex, invertedIndex, docs))
        }
        val similarityScoreMap: MutableMap<Int, Double> = HashMap()
        for (doc in docs) {
            val simScoreList = similarityScoreList.stream()
                    .map { x: Map<Int, Double> -> x.getOrDefault(doc, 0.0) }
                    .collect(Collectors.toList())
            similarityScoreMap[doc] = combineScore(simScoreList)
        }
        return similarityScoreMap
    }

    private fun combineScore(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0
        if (op == Operator.AND) {
            val scoreSum = scores.stream().mapToDouble { x: Double? -> (1 - x!!) * (1 - x) }.average().orElse(0.0)
            assert(scoreSum in 0.0..1.0)
            return 1 - sqrt(scoreSum)
        } else if (op == Operator.OR) {
            val scoreSum = scores.stream().mapToDouble { x: Double? -> x!! * x }.average().orElse(0.0)
            assert(scoreSum in 0.0..1.0)
            return sqrt(scoreSum)
        }
        return 0.0
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return "[" + queryList.stream()
                .map { obj: Query -> obj.toString() }
                .collect(Collectors.joining(" $op ")) + "]"
    }
}
