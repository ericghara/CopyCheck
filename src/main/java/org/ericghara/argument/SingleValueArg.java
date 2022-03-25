package org.ericghara.argument;

import lombok.NonNull;
import org.ericghara.argument.interfaces.ArgDefinitionInterface;
import org.ericghara.argument.interfaces.SingleValueArgInterface;

import java.util.List;

public class SingleValueArg extends ArgWithValues implements SingleValueArgInterface {

    public SingleValueArg(@NonNull ArgDefinitionInterface definition, List<String> values) {
        super(definition, values);
    }

    @Override
    public String value() {
        return values.size() > 0 ? values.get(0) : "";
    }
}