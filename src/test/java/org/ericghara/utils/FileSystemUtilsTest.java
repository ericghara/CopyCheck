package org.ericghara.utils;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {FileSystemUtils.class},
                properties = {"logging.level.org.ericghara=TRACE"} )
class FileSystemUtilsTest {

    @Autowired
    FileSystemUtils fsUtils;
    @TempDir
    Path tempDir;

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, delimiter='|', textBlock= """
            actualName | queryName | expected
            aDir       | aDir      | true
            a Dir      | a Dir     | true
            aDir       | bDir      | false
            """)
    void isDir(String actualName, String queryName, boolean expected) throws IOException {
        Path actualPath = tempDir.resolve(actualName).toAbsolutePath();
        Path queryPath = tempDir.resolve(queryName).toAbsolutePath();
        Files.createDirectory(actualPath);
        assertEquals(expected,  fsUtils.isDir(queryPath.toString() ) );
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, delimiter='|', textBlock= """
            actualName | queryName  | expected
            aFile.x    | aFile.x    | true
            a File.x   | a File.x   | true
            aFile.x    | bFile.x    | false
            """)
    void isFile(String actualName, String queryName, boolean expected) throws IOException {
        Path actualPath = tempDir.resolve(actualName).toAbsolutePath();
        Path queryPath = tempDir.resolve(queryName).toAbsolutePath();
        Files.createFile(actualPath);
        assertEquals(expected,  fsUtils.isFile(queryPath.toString() ) );
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, delimiter='|', textBlock= """
            actualName | expected
            afile      | false
            /afile     | true
            """)
    void isAbsolute(String pathString, boolean expected) {
        assertEquals(expected, fsUtils.isAbsolute(pathString) );
    }
}