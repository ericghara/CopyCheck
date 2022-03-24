package org.ericghara.configs;

import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.Id.EnumKey;
import org.ericghara.argument.SingleValueArgument;
import org.ericghara.utils.FileSystemUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import static org.ericghara.argument.Id.AppArg.*;
import static org.ericghara.argument.Id.ArgGroupKey.*;

@Configuration
@Slf4j
public class ArgsConfig {

    @Bean
    BiFunction<ArgDefinition, List<String>, SingleValueArgument> SingleValArgumentConstructor() {
        return SingleValueArgument::new;
    }

    @Bean
    ArgumentGroup<AppArg, ArgDefinition> OptionValidatorsBean() {
        ArgumentGroup<AppArg, ArgDefinition> args = new ArgumentGroup<>();

        Set<EnumKey> required = Set.of(REQUIRED);
        Set<EnumKey> mode = Set.of(MODE);
        Set<EnumKey> optional = Set.of(OPTIONAL);
        Set<EnumKey> optHashAlgo = Set.of(OPTIONAL,HASH_ALGO);

        args.add(SOURCE_FILE_LIST, new ArgDefinition(
                "srcFileList", null, 1, 1, required) );
        args.add(SOURCE, new ArgDefinition(
                "source", FileSystemUtils::isAbsolute, 1, 1, required) );
        args.add(DESTINATION, new ArgDefinition(
                "destination", FileSystemUtils::isDir, 1, 1, mode) );
        args.add(SNAPSHOT, new ArgDefinition(
                "snapshot", null, 0, 0, Set.of(MODE, DEFAULT) ) );
        // Hash algo names should correspond with: https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#messagedigest-algorithms
        // The argument name is what's being used to specify the hash algorithm.  The only exception is NO-HASH, which means only the existence of a file
        // will be tested.
        args.add(SHA_1, new ArgDefinition(
                "SHA-1", null, 0, 0, optHashAlgo) );
        args.add(MD5, new ArgDefinition(
                "MD5", null, 0, 0, optHashAlgo) );
        args.add(SHA_256, new ArgDefinition(
                "SHA-256", null, 0, 0, optHashAlgo) );
        args.add(NO_HASH, new ArgDefinition(
                "NO-HASH",  null, 0, 0, Set.of(OPTIONAL,HASH_ALGO,DEFAULT) ) );
        args.add(STD_LIST, new ArgDefinition(
                "STD-LIST", null, 0, 0, Set.of(LIST_FORMAT, DEFAULT) ) );
        args.seal();
        return args;
    }
}
