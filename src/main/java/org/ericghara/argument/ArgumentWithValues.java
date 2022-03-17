package org.ericghara.argument;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgDefinitionInterface;
import org.ericghara.argument.interfaces.ArgValuesInterface;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ArgumentWithValues implements ArgValuesInterface {

    @NonNull
    ArgDefinitionInterface definition;
    @NonNull
    List<String> values;


    @Override
    public Boolean validate(List<String> options) {
        return definition.validate(options);
    }

    @Override
    public String name() {
        return definition.name();
    }

    @Override
    public int minOptions() {
        return definition.minOptions();
    }

    @Override
    public int maxOptions() {
        return definition.maxOptions();
    }

    @Override
    public List<String> values() {
        return values;
    }

    @Override
    public Set<EnumKey> groups() {
        return definition.groups();
    }

    @Override
    public ArgDefinitionInterface definition() {
        return definition;
    }

}

