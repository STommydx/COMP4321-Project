package hk.ust.cse.comp4321.proj1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class SuggestionTrie extends Trie<SuggestionTrie.Node> {
    public static class Node {
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
