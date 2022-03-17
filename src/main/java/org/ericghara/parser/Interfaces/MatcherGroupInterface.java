package org.ericghara.parser.Interfaces;

import org.ericghara.argument.Id.EnumKey;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;

public interface MatcherGroupInterface<K extends EnumKey> {

    Set<K> getArgs();

    Collection<Matcher> getMatchers();

    Set<Entry<K, Matcher>> entrySet();

    Matcher get(K arg) throws NoSuchElementException;
}
