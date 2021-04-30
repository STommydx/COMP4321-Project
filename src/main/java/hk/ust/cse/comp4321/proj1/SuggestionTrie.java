package hk.ust.cse.comp4321.proj1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class SuggestionTrie extends Trie<SuggestionTrie.Node> {
    public static class Node {
        /**
         * This class stores the document frequency of the word represented by the {@link SuggestionTrie} storing this class's
         * instance, as well as {@code Map<String, Integer>} storing words from descendant {@link SuggestionTrie} and this
         * {@link SuggestionTrie} <b><i>TO</i></b> their respective document frequencies
         */
        private int documentFrequency;
        private Map<String, Integer> suggestions;

        public Node(int documentFrequency, Map<String, Integer> suggestions) {
            this.documentFrequency = documentFrequency;
            this.suggestions = suggestions;
        }

        public Node(int documentFrequency) {
            this.documentFrequency = documentFrequency;
            this.suggestions = new HashMap<>();
        }

        public int getDocumentFrequency() {
            return documentFrequency;
        }

        public void setDocumentFrequency(int documentFrequency) {
            this.documentFrequency = documentFrequency;
        }

        public Map<String, Integer> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(Map<String, Integer> suggestions) {
            this.suggestions = suggestions;
        }
    }

    /**
     * This method calls the {@link Trie#recursiveMerge recursiveMerge} with {@code selfFunc} to create a <i>placeholder</i>
     * {@link Node} storing a Map of prefix to document frequency.
     * {@code mergeFunc} maps the list storing descendants' nodes and 1 <i>placeholder</i> node from {@code selfFunc}, and
     * data/node of this trie, to a new {@link Node} with document frequency being the same as the data/node of this trie,
     * with {@code Map<String, Integer>} storing top {@code maxSize} amount of Map.Entry(word, document frequency of the word) with respect
     * to the document frequency of the word, where the words in the Map are from either descendants or this trie.
     * @param maxSize max suggestions size stored in {@link Node}
     */
    public void buildSuggestion(int maxSize) {
        recursiveMerge(
                (children, self) -> new Node(
                        self == null ? 0 : self.documentFrequency,
                        children.stream()
                                .flatMap(x -> x.suggestions.entrySet().stream())
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .limit(maxSize)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ),
                (key, node) -> {
                    Map<String, Integer> selfFreq = new HashMap<>();
                    if (node != null)
                        selfFreq.put(key, node.documentFrequency);
                    return new Node(0, selfFreq);
                }
        );
    }

    public Node put(String key, int freq) {
        return super.put(key, new Node(freq));
    }

}
