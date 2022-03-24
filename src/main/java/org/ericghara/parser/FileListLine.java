package org.ericghara.parser;

import org.ericghara.parser.Interfaces.FileHashInterface;

public record FileListLine(String text,
                           int lineNum,
                           boolean isMatch,
                           int[] pathRange,
                           int[] hashRange) implements FileHashInterface {

    public String path() {
        return getSubString(pathRange);
    }

    public String hash() {
        return getSubString(hashRange);
    }

    String getSubString(int[] region) {
        int start = region[0];
        int end = region[1];
        return text.substring(start, end);
    }
}
