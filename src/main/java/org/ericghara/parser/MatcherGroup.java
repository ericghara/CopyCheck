package org.ericghara.parser;

import lombok.NonNull;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.parser.Interfaces.MatcherGroupInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;


public class MatcherGroup<K extends EnumKey> implements MatcherGroupInterface<K> {

    private final Map<K,Matcher> matchers;

    @NonNull
    public MatcherGroup(List<Entry<K, Matcher>> argMatcherPairs) {
        matchers = addAll(argMatcherPairs);
    }

    private Map<K,Matcher> addAll(List<Entry<K, Matcher>> entries) throws IllegalStateException {
        Map<K,Matcher> matchers = new HashMap<>();
        entries.forEach( (e) ->
                matchers.put(e.getKey(), e.getValue() ) );
        return Map.copyOf(matchers);
    }

    @Override
    public Set<K> getArgs() {
        return matchers.keySet();
    }

    @Override
    public Collection<Matcher> getMatchers() {
        return matchers.values();
    }

    @Override
    public Set<Entry<K,Matcher>> entrySet() {
        return matchers.entrySet();
    }

    @Override
    public Matcher get(K arg) throws NoSuchElementException {
        var value = matchers.get(arg);
        if (Objects.isNull(value) ) {
            throw new NoSuchElementException("Unknown arg: " + arg);
        }
        return value;
    }

    @Override
    public int hashCode() {
        return matchers.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MatcherGroup<?> other) {
            return matchers.equals(other.matchers);
        }
        return false;
    }

    @Override
    public String toString() {
        return matchers.toString();
    }
}
