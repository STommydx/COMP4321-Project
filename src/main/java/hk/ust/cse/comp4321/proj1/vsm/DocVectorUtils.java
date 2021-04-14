package hk.ust.cse.comp4321.proj1.vsm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocVectorUtils {
    private DocVectorUtils() {}

    public static <T, U, R> Map<String, R> combine(Map<String, T> first, Map<String, U> second, BiFunction<T, U, R> combineFunction, T firstDefault, U secondDefault) {
        return Stream.of(first.keySet(), second.keySet())
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(x -> x, x -> combineFunction.apply(
                        first.getOrDefault(x, firstDefault),
                        second.getOrDefault(x, secondDefault)
                )));
    }

    public static <T, U, R> Map<String, R> combineIfExists(Map<String, T> first, Map<String, U> second, BiFunction<T, U, R> combineFunction) {
        Set<String> keySet = new HashSet<>(first.keySet());
        keySet.retainAll(second.keySet());
        return keySet.stream()
                .collect(Collectors.toMap(x -> x, x -> combineFunction.apply(
                        first.get(x),
                        second.get(x)
                )));
    }

    public static <T> Map<String, T> combine(Map<String, T> first, Map<String, T> second, BiFunction<T, T, T> combineFunction, T defaultValue) {
        return combine(first, second, combineFunction, defaultValue, defaultValue);
    }

    public static <T, R> Map<String, R> map(Map<String, T> v, Function<T, R> mappingFunction) {
        return v.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        x -> mappingFunction.apply(x.getValue())
                ));
    }

    public static Map<String, Double> add(Map<String, Double> first, Map<String, Double> second) {
        return combine(first, second, Double::sum, 0.);
    }

    public static Map<String, Double> elementwiseProduct(Map<String, Double> first, Map<String, Double> second) {
        return combineIfExists(first, second, (x, y) -> x * y);
    }

}
