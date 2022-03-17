package org.ericghara.argument.interfaces;

import java.util.List;

public interface ArgValuesInterface extends ArgDefinitionInterface {

    String name();

    List<String> values();

    ArgDefinitionInterface definition();

}
