package org.ericghara.argument;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgInterface;
import org.ericghara.argument.interfaces.EnumArgPairInterface;

import java.util.Map.Entry;

@AllArgsConstructor
public class EnumArgPair<K extends EnumKey, V extends ArgInterface> implements EnumArgPairInterface<K, V> {

    @NonNull
    final private K key;

    @NonNull
    final private V value;

    public EnumArgPair(Entry<K, V> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();

    }

    @Override
    public K getEnum() {
        return getKey();
    }

    @Override
    public V getArgData() {
        return getValue();
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("This method has not been implemented.");
    }


}
