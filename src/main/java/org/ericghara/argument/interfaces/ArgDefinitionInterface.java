package org.ericghara.argument.interfaces;

import org.ericghara.argument.Id.EnumKey;

import java.util.List;
import java.util.Set;

public interface ArgDefinitionInterface extends ArgInterface {

    Boolean validate(List<String> options);

    String name();

    int minOptions();

    int maxOptions();

    Set<EnumKey> groups();

}
