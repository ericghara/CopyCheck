package org.ericghara.checker;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.Id.ArgGroupKey;
import org.ericghara.argument.SingleValueArgument;
import org.ericghara.checker.interfaces.HashMatcher;
import org.ericghara.checker.interfaces.Hasher;
import org.ericghara.exceptions.ImproperApplicationArgumentsException;
import org.ericghara.exceptions.NoRecognizedFilesException;
import org.ericghara.parser.Interfaces.FileHashInterface;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.ericghara.argument.Id.AppArg.*;
import static org.ericghara.argument.Id.ArgGroupKey.*;

@Component
@Slf4j
public class FileChecker {

    private final Path source;
    private final Path destination;
    private final AppArg hashAlgo;
    private final String hashAlgoStr;
    private final HashMatcher matcher;
    private final Hasher hasher;
    private final Map<Boolean, List<HashPair>> results;
    

    public FileChecker(Stream<? extends FileHashInterface> lines,
                       FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs,
                       MatcherGroup<AppArg> matchers) {

        var required = foundArgs.getFound(REQUIRED);
        source = Paths.get(getValue(SOURCE, required) );
        destination = getDestination(foundArgs);
        this.hashAlgo = oneMatchOrThrows(HASH_ALGO, foundArgs);
        hashAlgoStr = getHashAlgoStr(foundArgs);
        hasher = getHasher();
        matcher = NormalMatcher::isMatch;
        results = checkAll(lines);
    }
    
    public List<HashPair> getValid() {
        return results.get(true);
    }
    
    public List<HashPair> getInvalid() {
        return results.get(false);
    }

    Map<Boolean, List<HashPair>> checkAll(Stream<? extends FileHashInterface> lines) {
        Map<Boolean,List<HashPair>> map = lines.map(this::hashALine)
                                               .collect(Collectors.groupingBy(this::checkALine));
        var t = map.putIfAbsent(true,
                new ArrayList<HashPair>() );
        var f = map.putIfAbsent(false,
                new ArrayList<HashPair>() );
        if (Objects.isNull(t) && Objects.isNull(f) ) {
            throw new NoRecognizedFilesException("FileChecker received an empty job.");
        }
        return map;
    }

    HashPair hashALine(FileHashInterface expectedHash) {
        String found;
        Path absoluteDest;
        try {
            Path relativeSource = source.relativize(Paths.get(expectedHash.path() ) );
            absoluteDest = destination.resolve(relativeSource)
                                      .toAbsolutePath();
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
            return new HashPair(expectedHash,
                    new FileHash(expectedHash.lineNum(), "???", "File Read Error"));
        }
        try {
            log.trace("Attempting to hash: " + absoluteDest);
            if (!Files.exists(absoluteDest) ) {
                throw new IllegalStateException();
            }
            found = hasher.hash(absoluteDest, hashAlgoStr);
        } catch(Exception e) {
            logHashFailure(absoluteDest);
            log.trace(e.toString() );
            return new HashPair(expectedHash,
                    new FileHash(expectedHash.lineNum(), absoluteDest.toString(), "Hash Failure"));
        }
        return new HashPair(expectedHash,
                new FileHash(expectedHash.lineNum(), absoluteDest.toString(), found));
    }

    boolean checkALine(HashPair pair) {
        var expected = pair.expected().hash();
        var found = pair.found().hash();
        return matcher.isMatch(expected, found);
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

    String getHashAlgoStr(FoundArgs<AppArg,ArgDefinition, SingleValueArgument> foundArgs) {
        return foundArgs.getAll()
                        .get(hashAlgo)
                        .name();
    }

    Hasher getHasher() {
        return hashAlgo == NO_HASH ? this::getBlank
                : this::getHexHash;
    }

    String getHexHash(@NonNull Path path, String hashAlgo) throws IOException {
        var digestUtils = new DigestUtils(hashAlgo);
        return digestUtils.digestAsHex(path);
    }

    String getBlank(Object o, Object p) {
        return "";
    }

    String getValue(AppArg argId, ArgumentGroup<AppArg, SingleValueArgument> group) {
        return group.get(argId).value();
    }

    Path getDestination(FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs) {
        // This method is key for configuring this instance for snapshot vs checker operation
        var mode = foundArgs.getFound(MODE);
        var destDef = mode.get(DESTINATION);
        if (Objects.nonNull(destDef) ) {
            return Paths.get(destDef.value() );
        }
        else if (mode.getArgIds().contains(SNAPSHOT) ) {
            return source;
        }
        throw new ImproperApplicationArgumentsException(
                "Unrecognized mode setting.  This is likely caused by an improper app configuration.");
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
    }
}
