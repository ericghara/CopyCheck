package org.ericghara.validators;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgDefinitionInterface;
import org.ericghara.argument.interfaces.ArgValuesInterface;
import org.ericghara.validators.interfaces.ArgumentValidatorInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.ericghara.argument.Id.ArgGroupKey.REQUIRED;

@Slf4j
@Component
public class ArgumentValidator<K extends EnumKey, V extends ArgDefinitionInterface, U extends ArgValuesInterface>
        implements ArgumentValidatorInterface {

    final private boolean isValid;

    @NonNull
    @Autowired(required = true)
    public ArgumentValidator(FoundArgs<K, V, U> foundArgs,
                             ApplicationArguments appArgs) {
        isValid = isValid(foundArgs, appArgs);
    }

    // for testing
    ArgumentValidator(){
        isValid = false;
    }

    boolean isValid(FoundArgs<K,V,U> foundArgs, ApplicationArguments appArguments) {
        var all = foundArgs.getAllDefined();
        log.debug("Received the following argument names: " + all);
        if (!allRecognized(all, appArguments)) {
            log.info("Found an unrecognized application argument name." );
            return false;
        }
        return hasRequired(foundArgs) &&
                validateAll(foundArgs);
    }

    public boolean isValid() {
        return isValid;
    }

    boolean hasRequired(FoundArgs<K,V,U> foundArgs) {
        return foundArgs.getAll(REQUIRED).size() ==
                foundArgs.getFound(REQUIRED).size();
    }

    boolean validateAll(FoundArgs<K, V, U> allFound) {
        var validity = allFound.getGroupFilters()
                                        .stream()
                                        .map(allFound::getFound)
                                        .map(this::validateGroup)
                                        .allMatch( (b) -> b.equals(true) );
        if (!validity) {
            log.debug("Unable to validate all groups.  Found an argument group");
        }
        return validity;
    }

    boolean validateGroup(ArgumentGroup<K,U> argGroup) {
        boolean validity = true;
        for ( var arg : argGroup.getArgs() ) {
            var options = arg.values();
            if (!arg.validate(options)) {
                validity = false;
                break;
            }
        }
        if(!validity) {
            log.info(format("Found an invalid argument option " +
                    "in the following argument group: %s", argGroup) );
        }
        log.trace(format("Validation status of group: %s: %b", argGroup, validity) );
        return validity;
    }

    boolean allRecognized(ArgumentGroup<K,V> allArgs, ApplicationArguments appArguments) {
        Set<String> foundNames = appArguments.getOptionNames();
        Set<String> allNames = allArgs.getNames();
        if (!allNames.containsAll(foundNames)) {
            var unknownNames = new HashSet<>(allNames);
            unknownNames.removeAll(allNames);
            log.info("Received unrecognized input arguments: "
                    + String.join(", ", unknownNames));
            return false;
        }
        log.trace(format("All groups in the set: %s, were recognized.", foundNames) );
        return true;
    }
}
