package org.ericghara.checker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.SingleValueArg;
import org.ericghara.exceptions.ImproperApplicationArgumentsException;
import org.ericghara.exceptions.UnrecoverableFileIOException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.BiConsumer;

import static java.lang.String.format;
import static org.ericghara.argument.Id.AppArg.*;
import static org.ericghara.argument.Id.ArgGroupKey.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultsPrinter implements ApplicationListener<ApplicationReadyEvent> {

    private final FileChecker checker;
    private final FoundArgs<AppArg,
            ArgDefinition, SingleValueArg> foundArgs;
    private String hashFormat;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var valid = checker.getValid();
        var invalid = checker.getInvalid();
        hashFormat = "%-" + selectHashSize() + "s";
        var printer = selectPrinter();
        printer.accept(valid, invalid);
    }

    BiConsumer<List<HashPair>, List<HashPair>> selectPrinter() {
        var mode = foundArgs.getFound(MODE).getArgIds();
        // generator signature is a subset of checker, so MUST check for checker first
        if (mode.contains(DESTINATION) ) {
            return this::checkerPrinter;
        }
        else if (mode.contains(SNAPSHOT) ) {
            return this::snapshotPrinter;
        }
        else {
            throw new ImproperApplicationArgumentsException(
                    "Could not determine signature for ResultsPrinter");
        }
    }

    int selectHashSize() {
        var hashAlgos = foundArgs.getFound(HASH_ALGO)
                                                .getArgIds()
                                                .iterator();
        if (!hashAlgos.hasNext() ) {
            throw new ImproperApplicationArgumentsException("Cannot configure ResultsPrinter.  No hashAlgo specified.");
        }
        var hashAlgo = hashAlgos.next();
        log.debug("Setting column width for " + hashAlgo + " hash algorithm");

        return switch (hashAlgo) {
            case NO_HASH -> 12;
            case MD5 -> 32;
            case SHA_1 -> 40;
            case SHA_256 -> 64;
            default -> throw new ImproperApplicationArgumentsException("Unrecognized Hash Algo: " + hashAlgo);
        };
    }

    void snapshotPrinter(List<HashPair> valid, List<HashPair> invalid) {
        if (invalid.isEmpty() ) {
            System.out.printf("Unrecoverable error.  Could not hash the following files:%n");
            invalid.forEach( (r) -> System.out.println(r.expected().path() ) );
            return;
        }
        var path = foundArgs.getFound(REQUIRED)
                                   .get(SOURCE_FILE_LIST)
                                   .value();
        File file = new File(path);
        if (file.exists() ) {
            throw new ImproperApplicationArgumentsException(
                    format("A file already exists at %s . Cannot overwrite an existing file.", path ) );
        }
        try (PrintWriter hashList = new PrintWriter(new BufferedWriter(new FileWriter(file) ) ) ){
            invalid.forEach( (r) -> hashList.printf("%s  %s%n", r.found().path(), r.found().hash() ) );
        } catch (Exception e) {
            throw new UnrecoverableFileIOException(format("Error while writing a srcFileList to: %s", path) );
        }
        System.out.printf("Success.  A srcFileList was written to: %s%n", path);
    }

    void checkerPrinter(List<HashPair> valid, List<HashPair> invalid) {
        System.out.printf("%n>> Valid << %n%n");
        System.out.printf(hashFormat + "  Path%n", "Hash");
        valid.forEach( (r) -> System.out.printf(hashFormat + "  %s%n", r.found().hash(), r.found().path()) );
        if (!invalid.isEmpty() ) {
            System.out.printf("%n>> Invalid << %n%n");
            System.out.printf(hashFormat + "  " + hashFormat + "  %s%n", "Expected", "Found", "Path");
            invalid.forEach((r) -> System.out.printf(hashFormat + "  " + hashFormat + "  %s%n",
                    r.expected().hash(), r.found().hash(), r.found().path()));
        }
        System.out.printf("%nSummary: %d of %d are valid%n", valid.size(), valid.size()+invalid.size() );
    }
}
