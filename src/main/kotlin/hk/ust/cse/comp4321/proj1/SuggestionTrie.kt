package hk.ust.cse.comp4321.proj1

class SuggestionTrie : Trie<SuggestionTrie.Node>() {

    /**
     * This class stores the document frequency of the word represented by the [SuggestionTrie] storing this class's
     * instance, as well as `Map<String, Integer>` storing words from descendant [SuggestionTrie] and this
     * [SuggestionTrie] ***TO*** their respective document frequencies
     */
    class Node(var documentFrequency: Int, var suggestions: Map<String, Int> = HashMap())

    /**
     * This method calls the [recursiveMerge][Trie.recursiveMerge] with `selfFunc` to create a *placeholder*
     * [Node] storing a Map of prefix to document frequency.
     * `mergeFunc` maps the list storing descendants' nodes and 1 *placeholder* node from `selfFunc`, and
     * data/node of this trie, to a new [Node] with document frequency being the same as the data/node of this trie,
     * with `Map<String, Integer>` storing top `maxSize` amount of Map.Entry(word, document frequency of the word) with respect
     * to the document frequency of the word, where the words in the Map are from either descendants or this trie.
     * @param maxSize max suggestions size stored in [Node]
     */
    fun buildSuggestion(maxSize: Int) {
        recursiveMerge(
                { children, self ->
                    Node(
                            self?.documentFrequency ?: 0,
                            children.flatMap { it?.suggestions?.entries ?: listOf() }
                                    .sortedByDescending { it.value }
                                    .take(maxSize)
                                    .associate { it.key to it.value }
                    )
                }, { key, node ->
            Node(0, if (node != null) mapOf(key to node.documentFrequency) else HashMap())
        }
        )
    }

    fun put(key: String, freq: Int): Node? {
        return super.put(key, Node(freq))
    }
}
