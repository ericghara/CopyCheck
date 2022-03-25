package org.ericghara.argument;

import lombok.AllArgsConstructor;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.interfaces.ArgDefinitionInterface;
import org.ericghara.argument.interfaces.ArgValuesInterface;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

@AllArgsConstructor
@Component
public class ArgWithValuesGroupGenerator<K extends EnumKey> {

    private final ApplicationArguments appArgs;

    // Provide with an argument group and any ArgValueInterface class constructor.
    // Uses constructor to convert input group into an ArgValue group output.  Keys remain
    // unchanged.
   public  <V extends ArgDefinitionInterface, U extends ArgValuesInterface>
    ArgumentGroup<K, U> convert(ArgumentGroup<K, V> nameGroup,
                                    BiFunction<V, List<String>, U> constructor) {

        // used lambda because method signature was out of control...
        Function<Entry<K, V>,
                 EnumArgPair<K,U>> converter = (pair) -> {
            K id = pair.getKey();
            V argDef = pair.getValue();
            var name = argDef.name();
            // For values auto-set to default.  Improvement opportunity, should be a way to
            // parameterize defaults instead of assuming all null up to here are defaults with no options;
            List<String> values = appArgs.containsOption(name) ?
                    appArgs.getOptionValues(argDef.name() ) :
                    List.of();

            return new EnumArgPair<>(id, constructor.apply(argDef, values) );
        };
        List<EnumArgPair<K,U>> entries = nameGroup.stream()
                                                  .map(converter)
                                                  .toList();
        return new ArgumentGroup<>(entries);
    }
}
