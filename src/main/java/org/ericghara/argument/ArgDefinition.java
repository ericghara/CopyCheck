package org.ericghara.argument;

import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgDefinitionInterface;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;

@Slf4j
public record ArgDefinition(String name,
                            Function<String, Boolean> validatorFunc,
                            int minOptions,
                            int maxOptions,
                            Set<EnumKey> groups) implements ArgDefinitionInterface {

    @Override
    public Boolean validate(List<String> options) {
        if (Objects.isNull(options) ) {
            log.info(format("The required argument: --%s, is missing.", name ) );
            return false;
        }
        if (!correctNumValues(options) ) {
            log.info(format("The argument: --%s, is contains an incorrect " +
                    "number of values.", name ) );
            return false;
        }

        boolean valid = minOptions <= 0 || options.stream()
                .allMatch(validatorFunc::apply);
        if (!valid) {
            log.info(format("Received an invalid argument " +
                    "option for the --%s argument.", name) );
        }
        return valid;
    }

    boolean correctNumValues(List<String> values) {
        int n = values.size();
        if (minOptions <= n && maxOptions >= n) {
            return true;
        }
        log.info(format("Received an invalid number of " +
                "options for the --%s argument", name) );
        return false;
    }
}
