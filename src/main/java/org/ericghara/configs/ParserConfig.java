package org.ericghara.configs;

import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.ArgWithValuesGroupGenerator;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.SingleValueArgument;
import org.ericghara.checker.FileChecker;
import org.ericghara.parser.FileListLine;
import org.ericghara.parser.FileListLineGenerator;
import org.ericghara.parser.FileListParser;
import org.ericghara.parser.MatcherGroup;
import org.ericghara.parser.Matchers;
import org.ericghara.validators.ArgumentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.ericghara.argument.Id.AppArg.*;
import static org.ericghara.argument.Id.ArgGroupKey.*;

@Configuration
@Slf4j
@DependsOn("argumentValidator")
public class ParserConfig {
    @Autowired
    ApplicationArguments appArgs;
    @Autowired
    ArgWithValuesGroupGenerator<AppArg> argGroupGenerator;
    @Autowired
    ArgumentGroup<AppArg, ArgDefinition> allArgs;
    @Autowired
    FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs;
    @Autowired
    ArgumentValidator<AppArg, ArgDefinition, SingleValueArgument> argumentValidator;
    @Autowired
    @Lazy
    FileListParser<SingleValueArgument> parser;

//    @Bean
//    @Order(1)
//    FoundArgs<AppArg,ArgDefinition, SingleValueArgument> foundArgsBean() {
//        return new FoundArgs<>(allArgs, appArgs, argGroupGenerator, SingleValueArgument::new);
//    }

//    @Bean
//    @Order(2)
//    ArgumentValidator<AppArg,ArgDefinition,SingleValueArgument> argumentValidator(
//            FoundArgs<AppArg,ArgDefinition,SingleValueArgument> foundArgs) {
//        return new ArgumentValidator<>(foundArgs, appArgs);
//    }

    @Bean
    @Order(200)
    MatcherGroup<AppArg> configMatchers(FoundArgs<AppArg,ArgDefinition, SingleValueArgument> foundArgs) {
        var source = foundArgs.getFound(REQUIRED)
                                      .get(SOURCE)
                                      .value();
        List<Entry<AppArg,Matcher>> entries = List.of(
                new SimpleImmutableEntry<>(MD5,Matchers.probableMD5() ),
                new SimpleImmutableEntry<>(SHA_1, Matchers.probableSHA1() ),
                new SimpleImmutableEntry<>(SHA_256, Matchers.probableSHA256() ),
                new SimpleImmutableEntry<>(NO_HASH, Matchers.matchEOL() ),
                new SimpleImmutableEntry<>(STD_LIST, Matchers.ChildPathsOfSmart(source) )
                );
        return new MatcherGroup<>(entries);
    }

    @Bean
    @Order(300)
    FileListLineGenerator fileListLineGeneratorBean(ArgumentValidator<AppArg, ArgDefinition, SingleValueArgument> validator,
                                                    MatcherGroup<AppArg> matchers,
                                                    FoundArgs<AppArg,ArgDefinition,SingleValueArgument> foundArgs) throws IllegalArgumentException{
        // change this to actually check to make sure one of the required is there;
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
    @Order(400)
    Stream<FileListLine> streamFileListLineBean(FileListLineGenerator lineGenerator) {
        var parser = new FileListParser<SingleValueArgument>(lineGenerator, foundArgs);
        return parser.matchStream();
    }

//    @Bean
//    @Order(500)
//    FileChecker fileCheckerBean(Stream<FileListLine> lines,
//                                FoundArgs<AppArg,ArgDefinition, SingleValueArgument> foundArgs,
//                                MatcherGroup<AppArg> matchers) {
//        return new FileChecker(lines,foundArgs, matchers);
//    }
//
//    @Bean
//    @Order(600)
//    ResultsPrinter resultsPrinterBean(FileChecker fileChecker) {
//        return new ResultsPrinter(fileChecker);
//    }
}
