package org.ericghara.argument;

import lombok.NonNull;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class ArgEntryGrouper<G, K extends EnumKey, V extends ArgInterface> {

    final private Map<G, ArgumentGroup<K,V>> groups;

    @NonNull
    public ArgEntryGrouper(ArgumentGroup<K, V> argGroup,
                   Function<EnumArgPair<K,V>, G> groupingFunc) {
        groups = findRequired(argGroup, groupingFunc);
    }

    public Optional<ArgumentGroup<K,V>> get(G group) {
        return Optional.of(groups.get(group) );
    }

    public Map<G, ArgumentGroup<K,V>> getResult() {
        return Map.copyOf(groups);
    }

    Map<G, ArgumentGroup<K,V>> findRequired(ArgumentGroup<K, V> argGroup,
                                            Function<EnumArgPair<K, V>, G> groupingFunc) {
        Map<G, List<EnumArgPair<K,V>>> raw =
                argGroup.stream()
                        .map(EnumArgPair::new)
                        .collect(groupingBy(groupingFunc, Collectors.toUnmodifiableList() ) );
        Map<G, ArgumentGroup<K,V>> groups =
                new HashMap<>(raw.size(), 1);
        raw.forEach( (key, value) ->
                groups.put(key, new ArgumentGroup<>(value) ) );
        return groups;
    }
}