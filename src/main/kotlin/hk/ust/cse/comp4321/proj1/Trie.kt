package hk.ust.cse.comp4321.proj1

import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

open class Trie<T>(protected var data: T? = null, protected val prefix: String = "") {
    protected val children: MutableMap<Char, Trie<T>> = HashMap()

    private fun map(key: String, mappingFunction: Function<T?, T?>, pos: Int): T? {
        if (pos == key.length) {
            val oldData = data
            data = mappingFunction.apply(oldData)
            return oldData
        }
        var childTrie = children[key[pos]]
        if (childTrie == null) {
            childTrie = Trie(null, key.substring(0, pos + 1))
            children[key[pos]] = childTrie
        }
        return childTrie.map(key, mappingFunction, pos + 1)
    }

    fun map(key: String, mappingFunction: Function<T?, T?>): T? {
        return map(key, mappingFunction, 0)
    }

    operator fun get(key: String): T? {
        return map(key) { it }
    }

    fun put(key: String, v: T): T? {
        return map(key) { v }
    }

    fun compute(key: String, func: Consumer<T?>): T? {
        return map(key) { func.accept(it); it }
    }

    /**
     * This method recursively runs at every children tries. At the deepest/lowest level of recursion at
     * the "leaf" trie(s), it maps the prefix of `String` type and data of `T` type of the "leaf"
     * trie to a `T` type object with function defined by `selfFunc` and stores it in a List. Then, the
     * List and data of the "leaf" trie are mapped with functional interface `mergeFunc` to data of the trie, and
     * get returned.
     * @param mergeFunc merge functional interface merging list of children data with this data
     * @param selfFunc refer to [buildSuggestion][SuggestionTrie.buildSuggestion] to get a better understand of this parameter
     * @return merged data
     */
    fun recursiveMerge(mergeFunc: BiFunction<List<T?>, T?, T?>, selfFunc: BiFunction<String, T?, T?>): T? {
        val resultList = children.values.map { it.recursiveMerge(mergeFunc, selfFunc) } + listOf(selfFunc.apply(prefix, data))
        data = mergeFunc.apply(resultList, data)
        return data
    }

    val all: Map<String, T>
        get() {
            val result = children.values
                    .flatMap { it.all.entries }
                    .filter { it.value != null }
                    .associate { it.key to it.value }
                    .toMutableMap()
            if (data != null) result[prefix] = data!!
            return result
        }

}
