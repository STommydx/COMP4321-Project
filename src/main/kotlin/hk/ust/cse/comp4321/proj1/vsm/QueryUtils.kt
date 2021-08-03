package hk.ust.cse.comp4321.proj1.vsm

import hk.ust.cse.comp4321.proj1.nlp.NLPUtils
import kotlin.math.sqrt

/**
 * The `QueryUtils` class is an utility class
 * that contains utility methods for handling user's query and similarity score calculation between the query and documents
 * to adopt vector space model in the search engine
 */
object QueryUtils {
    const val AND_TOKEN = "_AND_"
    const val OR_TOKEN = "_OR_"

    /**
     * Return a list of strings that stores the tokenized query
     * @param query user's input query
     * @return a list of tokenized query
     */
    fun tokenizeQuery(query: String): List<String> {
        val tokenizedQuery: MutableList<String> = ArrayList()
        var buffer = StringBuilder()
        var shouldEscape = false
        for (c in query.toCharArray()) {
            if (c == '"') {
                shouldEscape = !shouldEscape
            } else if (c == ' ' && !shouldEscape) {
                if (buffer.isNotEmpty()) tokenizedQuery.add(buffer.toString())
                buffer = StringBuilder()
            } else {
                buffer.append(c)
            }
        }
        if (buffer.length > 0) tokenizedQuery.add(buffer.toString())
        return tokenizedQuery
    }

    /**
     * Identify if the input token is a special token ("AND", "OR")
     * @param token string from the tokenized query
     * @return a string that indicates if the input token is "AND", "OR" or not
     */
    fun extractSpecialTokens(token: String): String {
        if (token.equals("AND", ignoreCase = true)) {
            return AND_TOKEN
        }
        return if (token.equals("OR", ignoreCase = true)) {
            OR_TOKEN
        } else token
    }

    /**
     * Preprocess the input query by extracting special tokens, stop word removal and stemming
     * @param query user's input query
     * @return a list of strings with preprocessed query
     */
    fun preprocessQuery(query: List<String>): List<String> {
        return query
                .map { extractSpecialTokens(it) }
                .filter { NLPUtils.stopwordFilter(it) }
                .map { NLPUtils.stemFilter(it) }
    }

    /**
     * Split the input the query to multiple subqueries
     * @param query user's input query
     * @param delimiter distinguisher between subqueries (such as "AND", "OR")
     * @return a list of lists of strings which stores the subqueies after splitting the input query
     */
    fun querySplit(query: List<String>, delimiter: String): List<List<String>> {
        val subQueries: MutableList<List<String>> = ArrayList()
        var buffer: MutableList<String> = ArrayList()
        for (queryWord in query) {
            if (queryWord == delimiter) {
                if (buffer.isNotEmpty()) {
                    subQueries.add(buffer)
                    buffer = ArrayList()
                }
            } else {
                buffer.add(queryWord)
            }
        }
        if (buffer.isNotEmpty()) subQueries.add(buffer)
        return subQueries
    }

    /**
     * Calculate Cosine Similarity between a document and query
     * @param v1 a map between a keyword and weight
     * @param v2 a map between a keyword and weight
     * @return a floating point value after calculating Cosine Similarity
     */
    @JvmStatic
    fun cosineSimilarity(v1: Map<String, Double>, v2: Map<String, Double>): Double {
        return dot(v1, v2) / l2Norm(v1) / l2Norm(v2)
    }

    @JvmStatic
    fun dot(v1: Map<String, Double>, v2: Map<String, Double>): Double {
        val vProduct = DocVectorUtils.elementwiseProduct(v1, v2)
        return vProduct.values.stream().reduce(0.0) { l, r -> l + r }
    }

    /**
     * Normalize the similarity score by l2 normalization method
     * @param v a map between keyword and weight
     * @return a l2 normalized floating point value
     */
    @JvmStatic
    fun l2Norm(v: Map<String, Double>): Double {
        return sqrt(v.values.stream().mapToDouble { it * it }.sum())
    }
}
