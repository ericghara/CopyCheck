package org.ericghara.configs;

import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.SingleValueArg;
import org.ericghara.checker.FileChecker;
import org.ericghara.exceptions.ImproperApplicationArgumentsException;
import org.ericghara.parser.FileListLineGenerator;
import org.ericghara.parser.FileListParser;
import org.ericghara.parser.FileLister;
import org.ericghara.parser.Interfaces.FileHashInterface;
import org.ericghara.parser.MatcherGroup;
import org.ericghara.parser.Matchers;
import org.ericghara.validators.ArgumentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.ericghara.argument.Id.AppArg.*;
import static org.ericghara.argument.Id.ArgGroupKey.*;

@Configuration
@Slf4j
public class AppConfig {

    @Autowired
    FileLister fileLister;
    @Autowired
    FoundArgs<AppArg, ArgDefinition, SingleValueArg> foundArgs;

    @Bean
    MatcherGroup<AppArg> configMatchers(FoundArgs<AppArg,ArgDefinition, SingleValueArg> foundArgs) {
        var source = foundArgs.getFound(REQUIRED)
                                      .get(SOURCE)
                                      .value();
        List<Entry<AppArg,Matcher>> entries = List.of(
                new SimpleImmutableEntry<>(MD5, Matchers.probableMD5() ),
                new SimpleImmutableEntry<>(SHA_1, Matchers.probableSHA1() ),
                new SimpleImmutableEntry<>(SHA_256, Matchers.probableSHA256() ),
                new SimpleImmutableEntry<>(NO_HASH, Matchers.matchEOL() ),
                new SimpleImmutableEntry<>(STD_LIST, Matchers.ChildPathsOfSmart(source) )
                );
        return new MatcherGroup<>(entries);
    }

    @Bean
    FileListLineGenerator fileListLineGeneratorBean(ArgumentValidator<AppArg, ArgDefinition, SingleValueArg> validator,
                                                    MatcherGroup<AppArg> matchers,
                                                    FoundArgs<AppArg,ArgDefinition, SingleValueArg> foundArgs) throws IllegalArgumentException{
        if (!validator.isValid() ) {
            log.info("Invalid command line arguments.  Application startup failed.");
            System.out.println("Application startup failed.  See log.");
            throw new IllegalArgumentException("Invalid command line arguments.  See logs for details.");
        }

        Matcher source = matchers.get(
                FileChecker.oneMatchOrThrows(LIST_FORMAT, foundArgs) );
        Matcher hash = matchers.get(
                FileChecker.oneMatchOrThrows(HASH_ALGO, foundArgs) );
        return new FileListLineGenerator(source, hash);
    }

    @Bean
    Stream<? extends FileHashInterface> streamFileListLineBean(FileListLineGenerator lineGenerator) {
        var mode = foundArgs.getFound(MODE).getArgIds();
        if (mode.contains(SNAPSHOT) ) {
            return fileLister.stream();
        }
        else if (mode.contains(DESTINATION) ) {
            var parser = new FileListParser<SingleValueArg>(lineGenerator, foundArgs);
            return parser.matchStream();
        }
        throw new ImproperApplicationArgumentsException("Unrecognized mode setting");
    }
}
