package org.ericghara.parser;

public record FileListLine(String text,
                    int lineNum,
                    boolean isMatch,
                    int[] pathRange,
                    int[] hashRange) {

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
