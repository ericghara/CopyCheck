package org.ericghara.checker;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.ArgEntryGrouper;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.EnumArgPair;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.Id.ArgGroupKey;
import org.ericghara.argument.SingleValueArgument;
import org.ericghara.checker.interfaces.HashMatcher;
import org.ericghara.exceptions.ImproperApplicationArgumentsException;
import org.ericghara.exceptions.NoRecognizedFilesException;
import org.ericghara.parser.FileListLine;
import org.ericghara.parser.MatcherGroup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.ericghara.argument.Id.AppArg.*;
import static org.ericghara.argument.Id.ArgGroupKey.HASH_ALGO;
import static org.ericghara.argument.Id.ArgGroupKey.REQUIRED;

@Component
@Slf4j
public class FileChecker {

    private final Path source;
    private final Path destination;
    private final String hashAlgoStr;
    private final AppArg hashAlgo;
    final HashMatcher matcher;
    final Map<Boolean, List<FileListLine>> results; 
    

    public FileChecker(Stream<FileListLine> lines,
                       FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs,
                       MatcherGroup<AppArg> matchers) {
        var required = foundArgs.getFound(REQUIRED);
        source = Paths.get(getValue(SOURCE, required) );
        destination = Paths.get(getValue(DESTINATION, required) );
        this.hashAlgo = oneMatchOrThrows(HASH_ALGO, foundArgs);
        hashAlgoStr = foundArgs.getAll()
                               .get(hashAlgo)
                               .name();
        matcher = getHashMatcher(hashAlgo);
        results = checkAll(lines);
    }
    
    public List<FileListLine> getValid() {
        return results.get(true);
    }
    
    public List<FileListLine> getInvalid() {
        return results.get(false);
    }
    
    

    Map<Boolean, List<FileListLine>> checkAll(Stream<FileListLine> lines) {
        Map<Boolean,List<FileListLine>> map = lines.collect(Collectors.groupingBy(this::checkALine));
        var t = map.putIfAbsent(true, new ArrayList<FileListLine>() );
        var f = map.putIfAbsent(false, new ArrayList<FileListLine>() );
        if (Objects.isNull(t) && Objects.isNull(f) ) {
            throw new NoRecognizedFilesException("FileChecker received an empty job.");
        }
        return map;
    }

    boolean checkALine(FileListLine line) {
        String found;
        Path absoluteDest;
        try {
            Path relativeSource = source.relativize(Paths.get(line.path() ) );
            absoluteDest = destination.resolve(relativeSource);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                log.debug("Could not get Hash.  Likely unable to relativize paths " +
                        "(indicative of usage or parsing error.)" );
                e.printStackTrace();
            }
            else {
                log.debug("unknown exception");
                e.printStackTrace();
            }
            return false;
        }
        try {
            log.trace("Attempting to hash: " + absoluteDest);
            if (!Files.exists(absoluteDest) ) {
                throw new IllegalStateException();
            }
            found = hashAlgo == NO_HASH ? getBlank(absoluteDest, hashAlgoStr)
                    : getHexHash(absoluteDest, hashAlgoStr);
        } catch(Exception e) {
            logHashFailure(absoluteDest);
            log.trace(e.toString() );
            return false;
        }
        return matcher.isMatch(line.hash(), found);
    }
    
    private void logHashFailure(Path absoluteDest) {
        var regFile = false;
        try {
            log.trace("Performing regular file check on: " + absoluteDest);
            regFile = Files.isRegularFile(absoluteDest);
        } catch (Exception e) {
            log.trace("Regular file check failed for: " + absoluteDest);
        }
        if (regFile) {
            log.info("IO Failure while checking: " + absoluteDest);
        } else {
            log.info(format("Failure reading destination file: %s.", absoluteDest));
        }
    } 


    String getHexHash(@NonNull Path path, String hashAlgo) throws IOException {
        var digestUtils = new DigestUtils(hashAlgo);
        return digestUtils.digestAsHex(path);
    }

    String getBlank(Object o, Object p) {
        return "";
    }

    HashMatcher getHashMatcher(AppArg hashAlgo) {
        return hashAlgo.equals(NO_HASH) ?
                AlwaysTrue::isMatch : NormalMatcher::isMatch;
    }


    String getValue(AppArg argId, ArgumentGroup<AppArg, SingleValueArgument> group) {
        return group.get(argId).value();
    }

    String getHashAlgo(ArgumentGroup<AppArg, SingleValueArgument> optional) {
        Supplier<ImproperApplicationArgumentsException> unspecifiedAlgo = () ->
                new ImproperApplicationArgumentsException("No Hash Algorithm was specified");

        Function<EnumArgPair<AppArg, SingleValueArgument>, Boolean>
                matchHashGp = (pair) -> pair.getArgData()
                                            .groups()
                                            .contains(HASH_ALGO);
        ArgEntryGrouper<Boolean, AppArg, SingleValueArgument> grouper =
                new ArgEntryGrouper<>(optional, matchHashGp);
        var algos =
                grouper.get(true).orElseThrow(unspecifiedAlgo);
        var matches = algos.getArgIds();
        if (matches.size() > 1) {
            throw new ImproperApplicationArgumentsException("Multiple hash algorithms were specified");
        }
        var  arg = matches.stream()
                                   .findFirst()
                                   .orElseThrow(unspecifiedAlgo);
        return optional.get(arg)
                       .name();
    }

    static public AppArg oneMatchOrThrows(ArgGroupKey key,
                                   FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs) {
        Set<AppArg> setArgs = foundArgs.getFound(key)
                .getArgIds();
        if (setArgs.size() != 1) {
            throw new ImproperApplicationArgumentsException(
                    "Missing or conflicting arguments in arg group: " + key);
        }
        return setArgs.iterator()
                .next();
    };
}
