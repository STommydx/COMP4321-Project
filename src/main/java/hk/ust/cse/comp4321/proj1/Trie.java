package hk.ust.cse.comp4321.proj1;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Trie<T> {

    protected final String prefix;
    protected final Map<Character, Trie<T>> children;
    protected @Nullable T data;

    private Trie(@Nullable T data, String prefix) {
        this.prefix = prefix;
        this.children = new HashMap<>();
        this.data = data;
    }

    public Trie(@Nullable T data) {
        this(data, "");
    }

    public Trie() {
        this(null);
    }

    private @Nullable T map(String key, Function<T, T> mappingFunction, int pos) {
        if (pos == key.length()) {
            T oldData = data;
            data = mappingFunction.apply(oldData);
            return oldData;
        }
        Trie<T> childTrie = children.get(key.charAt(pos));
        if (childTrie == null) {
            childTrie = new Trie<>(null, key.substring(0, pos + 1));
            children.put(key.charAt(pos), childTrie);
        }
        return childTrie.map(key, mappingFunction, pos + 1);
    }

    public @Nullable T map(String key, Function<T, T> mappingFunction) {
        return map(key, mappingFunction, 0);
    }

    public @Nullable T get(String key) {
        return map(key, x -> x);
    }

    public @Nullable T put(String key, T v) {
        return map(key, x -> v);
    }

    public @Nullable T compute(String key, Consumer<T> func) {
        return map(key, x -> {
            func.accept(x);
            return x;
        });
    }

    /**
     * This method recursively runs at every children tries. At the deepest/lowest level of recursion at
     * the "leaf" trie(s), it maps the prefix of <code>String</code> type and data of <code>T</code> type of the "leaf"
     * trie to a <code>T</code> type object with function defined by {@code selfFunc} and stores it in a List. Then, the
     * List and data of the "leaf" trie are mapped with functional interface {@code mergeFunc} to data of the trie, and
     * get returned.
     * @param mergeFunc merge functional interface merging list of children data with this data
     * @param selfFunc refer to {@link SuggestionTrie#buildSuggestion buildSuggestion} to get a better understand of this parameter
     * @return merged data
     */
    public @Nullable T recursiveMerge(BiFunction<List<T>, T, T> mergeFunc, BiFunction<String, T, T> selfFunc) {
        List<T> resultList = children.values().stream()
                .map(x -> x.recursiveMerge(mergeFunc, selfFunc))
                .collect(Collectors.toList());
        resultList.add(selfFunc.apply(prefix, data));
        data = mergeFunc.apply(resultList, data);
        return data;
    }

    public Map<String, T> getAll() {
        Map<String, T> resultList = children.values().stream()
                .flatMap(x -> x.getAll().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (data != null) resultList.put(prefix, data);
        return resultList;
    }

}
