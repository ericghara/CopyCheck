package org.ericghara.parser;

import lombok.extern.slf4j.Slf4j;
import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.ArgumentWithValues;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.SingleValueArgument;
import org.ericghara.exceptions.UnrecoverableFileIOException;
import org.ericghara.utils.FileSystemUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.ericghara.argument.Id.AppArg.SOURCE_FILE_LIST;
import static org.ericghara.argument.Id.ArgGroupKey.REQUIRED;

@Component
@Slf4j
public class FileListParser<V extends ArgumentWithValues> {

    private final FileListLineGenerator generator;
    private final File sourceFileList;

    public FileListParser(FileListLineGenerator generator,
                          FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs) {
        this.generator = generator;
        sourceFileList = FileSystemUtils.getFile(getValue(SOURCE_FILE_LIST, foundArgs.getFound(REQUIRED) ) );
    }

    Stream<String> lineTextStream(Scanner scanner) {
        Stream.Builder<String> sb = Stream.builder();
        while (scanner.hasNextLine() ) {
            sb.accept(scanner.nextLine() );
        }
        return sb.build();
    }

    public Stream<FileListLine> matchStream() throws UnrecoverableFileIOException {
        try (var scanner = new Scanner(sourceFileList) ) {
            return lineTextStream(scanner).map( (generator::generate) )
                                          .filter(FileListLine::isMatch);

        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                log.info(format("Could not open the sourceFileList: %s.", sourceFileList));
            }
            else  {
                log.info(format("Read error after opening sourceFileList %s.", sourceFileList) );
            }
            throw new UnrecoverableFileIOException("Unable to read sourceFileList", e);
        }
    }

    String getValue(AppArg argId, ArgumentGroup<AppArg,SingleValueArgument> group) {
        return group.get(argId)
                    .value();
    }
}
