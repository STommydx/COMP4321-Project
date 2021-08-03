package hk.ust.cse.comp4321.proj1.vsm

import java.util.function.BiFunction
import java.util.function.Function

/**
 * The `DocVectorUtils` class is an utility class
 * that contains utility methods for handling document vectors
 * to adopt vector space model in the search engine
 */
object DocVectorUtils {

    fun <T, U, R> combine(first: Map<String, T>, second: Map<String, U>, combineFunction: BiFunction<T, U, R>, firstDefault: T, secondDefault: U): Map<String, R> {
        return first.keys.plus(second.keys)
                .associateWith { combineFunction.apply(first[it] ?: firstDefault, second[it] ?: secondDefault) }
    }

    fun <T> combine(first: Map<String, T>, second: Map<String, T>, combineFunction: BiFunction<T, T, T>, default: T): Map<String, T> {
        return combine(first, second, combineFunction, default, default)
    }

    fun <T, U, R> combineIfExists(first: Map<String, T>, second: Map<String, U>, combineFunction: BiFunction<T, U, R>): Map<String, R> {
        val keySet: MutableSet<String> = HashSet(first.keys)
        keySet.retainAll(second.keys)
        return keySet.associateWith { combineFunction.apply(first[it]!!, second[it]!!) }
    }

    @JvmStatic
    fun <T, R> map(v: Map<String, T>, mappingFunction: Function<T, R>): Map<String, R> {
        return v.mapValues { mappingFunction.apply(it.value) }
    }

    @JvmStatic
    fun add(first: Map<String, Double>, second: Map<String, Double>): Map<String, Double> {
        return combine(first, second, { x, y -> x + y }, 0.0)
    }

    @JvmStatic
    fun elementwiseProduct(first: Map<String, Double>, second: Map<String, Double>): Map<String, Double> {
        return combineIfExists(first, second) { x, y -> x * y }
    }

}
