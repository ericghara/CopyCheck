package org.ericghara.argument;

import lombok.Getter;
import lombok.NonNull;
import org.ericghara.argument.Id.ArgGroupKey;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgDefinitionInterface;
import org.ericghara.argument.interfaces.ArgValuesInterface;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.ericghara.argument.Id.ArgGroupKey.*;

@Getter
@Component
public class FoundArgs<K extends EnumKey, V extends ArgDefinitionInterface, U extends ArgValuesInterface> {

    static final List<ArgGroupKey> GROUP_FILTERS =
            List.of(REQUIRED, MODE, HASH_ALGO, LIST_FORMAT);

    private Map<ArgGroupKey, ArgumentGroup<K,V>> sortedAll;
    private Map<ArgGroupKey, ArgumentGroup<K,U>> sortedFound;
    private ArgumentGroup<K, V> all;

    public FoundArgs(@NonNull ArgumentGroup<K, V> all,
                     ApplicationArguments appArgs,
                     ArgWithValuesGroupGenerator<K> generator,
                     BiFunction<V, List<String>, U>  constructor) {
        this.all = all;
        sortedAll = partitionBy(all, GROUP_FILTERS);
        sortedFound = foundOrDefault(appArgs, generator, constructor);
    }

    public ArgumentGroup<K, V> getAll(ArgGroupKey groupId) throws NoSuchElementException {
        var gp = sortedAll.get(groupId);
        if (Objects.isNull(gp) ) {
            throw new NoSuchElementException("Invalid groupId: " + groupId);
        }
        return gp;
    }

    public ArgumentGroup<K, U> getFound(ArgGroupKey groupId) throws NoSuchElementException {
        var gp = sortedFound.get(groupId);
        if (Objects.isNull(gp) ) {
            throw new NoSuchElementException("Invalid groupId: " + groupId);
        }
        return gp;
    }

    public ArgumentGroup<K, V> getAllDefined() {
        return all;
    }

    public List<ArgGroupKey> getGroupFilters() {
        return GROUP_FILTERS;
    }

    /* Creates map of found Arguments by gpKey.  If no arguments
    are found for a gpKey creates argument gp of found defaults or empty
    group if no defaults;
     */
    Map<ArgGroupKey,ArgumentGroup<K,U>> foundOrDefault(ApplicationArguments appArgs,
                                                       ArgWithValuesGroupGenerator<K> generator,
                                                       BiFunction<V, List<String>, U> constructor) {
        Map<ArgGroupKey, ArgumentGroup<K,U>> foundOrDefault = new HashMap<>();
        for (var gpKey : sortedAll.keySet()) {
            ArgumentGroup<K, V> allGp = sortedAll.get(gpKey);
            Map<Boolean, ArgumentGroup<K,V>> foundGp = new
                    ArgEntryGrouper<>(allGp, sortByFound(appArgs) ).getResult();
            Map<Boolean, ArgumentGroup<K,V>> defaultGp = new
                    ArgEntryGrouper<>(allGp, sortByDefault() ).getResult();
            var toAdd = foundGp.get(true);
            if (Objects.isNull(toAdd) ) {
                toAdd = defaultGp.containsKey(true) ? defaultGp.get(true) : new ArgumentGroup<>();
                toAdd.seal();
            }
            foundOrDefault.put(gpKey, generator.convert(toAdd, constructor) );
        }
        return foundOrDefault;
    }

//    Use ArgWithValuesGroupGenerator
//    ArgumentGroup<K,U> argGroupConverter(ArgumentGroup<K,V> group,
//                                         ApplicationArguments appArgs,
//                                         BiFunction<V, List<String>, U> constructor) {
//        List<EnumArgPair<K,U>> entries = group.stream().map( (entry) -> {
//            var k = entry.getKey();
//            var arg = entry.getValue();
//            var argValues = appArgs.getOptionValues(arg.name() );
//            return new EnumArgPair<>(k, constructor.apply(arg, argValues) );
//        }).toList();
//        return new ArgumentGroup<>(entries);
//    }

    Map<ArgGroupKey, ArgumentGroup<K, V>> partitionBy(ArgumentGroup<K, V> parentGroup, List<ArgGroupKey> sortKeys) {
        var grouper = new ArgEntryGrouper<ArgGroupKey, K,V>(parentGroup,
                sortKeyGrouper(sortKeys) );
        return grouper.getResult();
    }

    Function<EnumArgPair<K, V>, Boolean> sortByFound(ApplicationArguments appArgs) {
        return (EnumArgPair<K, V> p) -> {
            var name = p.getValue().name();
            return appArgs.containsOption(name);
        };
    }

    Function <EnumArgPair<K, V>, Boolean> sortByDefault() {
        return (EnumArgPair<K,V> p)  -> {
            return p.getValue()
                    .groups()
                    .contains(DEFAULT);
        };
    }

    Function<EnumArgPair<K, V>, ArgGroupKey> sortKeyGrouper(List<ArgGroupKey> sortKeys) {
        return (EnumArgPair<K, V> p) -> {
            for (var k : sortKeys) {
                if (p.getArgData().groups().contains(k)) {
                    return k;
                }
            }
            return null;
        };
    }
}
