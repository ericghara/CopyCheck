package org.ericghara.parser;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.SingleValueArgument;
import org.ericghara.checker.FileHash;
import org.ericghara.exceptions.ImproperApplicationArgumentsException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.ericghara.argument.Id.AppArg.SOURCE;
import static org.ericghara.argument.Id.ArgGroupKey.REQUIRED;

@Component
@Slf4j
@AllArgsConstructor
public class FileLister {

    private final FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs;
    
    public Stream<FileHash> stream() {
        var visitor = new FileVisitor();
        var source = getSource();
        try {
            Files.walkFileTree(source ,visitor);
        } catch (Exception e) {
            System.out.println("Suppressed an exception in SimpleFileVisitor.FileVisit.");
            e.printStackTrace();
        }
        if (visitor.getIgnoredFiles() > 0 || visitor.getIgnoredFolders() > 0) {
            log.info(format("Read Errors occurred! %d files and %d folders were excluded from the snapshot",
                    visitor.getIgnoredFiles(), visitor.getIgnoredFolders() ) );
        }
        else {
            log.info("Successfully read in all subfiles of " + source);
        }
        return visitor.stream();
    }

    Path getSource() {
        String source;
        try {
            source = foundArgs.getFound(REQUIRED)
                    .get(SOURCE)
                    .value();
        } catch (Exception e) {
            throw new ImproperApplicationArgumentsException("Unable to read source path", e);
        }
        return Paths.get(source);
    }

    // Does not follow symlinks.  Does not add folders (but visits them to discover files)
    static class FileVisitor extends SimpleFileVisitor<Path> {

        Stream.Builder<FileHash> walkStream = Stream.builder();
        int ignoredFiles = 0;
        int ignoredFolders = 0;
        int curFile = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            var fileHash = new FileHash(curFile++, file.toString(), "");
            walkStream.add(fileHash);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            ignoredFiles++;
            log.info("Unable to read file: " + file + " it will be excluded from the snapshot");
            log.debug(exc.toString() );
            exc.addSuppressed(new IOException() );
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            if (Objects.nonNull(exc) ) {
                ignoredFolders++;
                exc.addSuppressed(new IOException() );
                log.info("Unable to open directory: " + dir + " its contents will be excluded from the snapshot");
            }
            return FileVisitResult.CONTINUE;
        }

        int getIgnoredFiles() {
            return ignoredFiles;
        }

        int getIgnoredFolders() {
            return ignoredFolders;
        }

        Stream<FileHash> stream() {
            return walkStream.build();
        }
    }
}
