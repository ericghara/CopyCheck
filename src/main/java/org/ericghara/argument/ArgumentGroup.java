package org.ericghara.argument;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class ArgumentGroup<K extends EnumKey, V extends ArgInterface> {

    boolean sealed;
    HashMap<K, V> argMap;
    HashMap<String, K> nameEnumMap;

    public ArgumentGroup() {
        sealed = false;
        argMap = new HashMap<>();
        nameEnumMap = new HashMap<>();
    }

    //Creates a sealed argument group from EnumArgPairs
    public ArgumentGroup(Collection<EnumArgPair<K,V>> entries) {
        this();
        addAll(entries);
        seal();
    }

    public void  addAll(@NonNull Collection<EnumArgPair<K, V>> entries) throws UnsupportedOperationException {
        entries.forEach( (e) -> add(e.getEnum(), e.getArgData() ) );;
    }

    public void add(K argKey, @NonNull V argData) throws UnsupportedOperationException {
        if (sealed) {
            log.info(String.format("Attempted addition to a sealed group " +
                    "is not allowed.  Arg: %s", argKey));
            throw new UnsupportedOperationException("Cannot modify to a sealed group.");
        }
        V argInSet = argMap.putIfAbsent(argKey, argData);
        if (Objects.nonNull(argInSet)) {
            log.info("Attempted to add an Arg which is already defined.  Arg: " + argKey);
            throw new UnsupportedOperationException("Cannot overwrite an existing {ArgKey, ArgData} pair");
        }
        K enumInSet = nameEnumMap.putIfAbsent(argData.name(), argKey);
        if (Objects.nonNull(enumInSet)) {
            log.info("Name conflict: two args have the same name.  Arg: " + argKey);
            throw new UnsupportedOperationException("Cannot group arguments with the same name.");
        }
    }

    public Stream<Entry<K, V>> stream() {
        return argMap.entrySet()
                     .stream();
    }

    public V get(K argKey) {
        return argMap.get(argKey);
    }

    public V get(String argName) {
        return get(getArgId(argName) );
    }

    public K getArgId(String argName) {
        return nameEnumMap.get(argName);
    }

    public Set<K> getArgIds() {
        return Set.copyOf(argMap.keySet());
    }

    public int size() {
        return argMap.size();
    }

    public Set<String> getNames() {
        return Set.copyOf(nameEnumMap.keySet() );
    }

    public Set<V> getArgs() {
        return Set.copyOf(argMap.values() );
    }

    @Override
    public String toString() {
        return nameEnumMap.keySet()
                          .toString();
    }

    public void seal() {
        sealed = true;
    }

    public boolean isSealed() {
        return sealed;
    }
}
