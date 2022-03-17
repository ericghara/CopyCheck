package org.ericghara.argument.interfaces;

import org.ericghara.argument.Id.EnumKey;

import java.util.Map;

public interface EnumArgPairInterface<K extends EnumKey, V extends ArgInterface> extends Map.Entry<K, V> {

    K getEnum() throws IllegalStateException;

    V getArgData() throws IllegalStateException;




}
