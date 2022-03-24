package org.ericghara.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

@Slf4j
public class FileSystemUtils {

    @NonNull
    public static boolean isAbsolute(String pathString) {
        Path path = Paths.get(pathString);
        return path.isAbsolute();
    }

    @NonNull
    public static boolean isDir(String pathString) {
        Path path = Paths.get(pathString);
        try {
            return Files.isDirectory(path, NOFOLLOW_LINKS);  //Add NO_FOLLOW_LINKS to disable symlinks
        } catch (SecurityException e){
            log.trace(e.getMessage() );
            log.info(String.format("Insufficient permissions to open the dir: %s", pathString) );
            return false;
        }
    }

    @NonNull
    public static boolean isFile(String pathString) {
        Path path = Paths.get(pathString);
        try {
            return Files.isRegularFile(path, NOFOLLOW_LINKS);
        } catch(SecurityException e) {
            log.trace(e.getMessage() );
            log.info(String.format("Insufficient permissions to open the file: %s", pathString) );
            return false;
        }
    }

    @NonNull
    public static File getFile(String pathStr) {
        return new File(pathStr);
    }
}
