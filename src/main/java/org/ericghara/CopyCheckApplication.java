package org.ericghara;

import lombok.AllArgsConstructor;
import org.ericghara.exceptions.ImproperApplicationArgumentsException;
import org.ericghara.exceptions.NoRecognizedFilesException;
import org.ericghara.exceptions.UnrecoverableFileIOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class CopyCheckApplication {

    static final String USAGE = """
    ****Proper usage goes here****""";

    static final String IMPROPER_ARGUMENTS = """
            ***
    Message goes here!!!!
            """;
    static final String FILE_IO = """
            A read error of a required file or directory for application start-up occurred.
            Ensure you have correct access permissions.
            """;
    static final String EMPTY_JOB = """
            Error parsing the source file list.  No file hashcode pairs were found, that match the specified hash algorithm.
            To only check if a file exists at the destination, use the NO-HASH argument.
            """;
    static final String CATCH_ALL = """
            An error occurred.  See application logs.  Run with --debug or --trace for additional info.
            """;

    public static void main(String[] args) {

        try {
            SpringApplication.run(CopyCheckApplication.class, args);
        } catch (Exception e ) {
            {
                var message = "";
                if (e instanceof ImproperApplicationArgumentsException) {
                    System.out.println(IMPROPER_ARGUMENTS);
                    System.out.println(USAGE);
                }
                else if (e instanceof UnrecoverableFileIOException) {
                    System.out.println(FILE_IO);
                }
                else if (e instanceof NoRecognizedFilesException) {
                    System.out.println(EMPTY_JOB);
                }
                else {
                    e.printStackTrace();
                    System.out.println(CATCH_ALL);
                }
            }
        }
    }
}