package org.ericghara.parser;

import java.util.regex.Matcher;


public class FileListLineGenerator {

    private int lineN = 0;
    private final Matcher path;
    private final Matcher hash;

    public FileListLineGenerator(Matcher path, Matcher hash) {
        this.path = path;
        this.hash = hash;
    }
    
    public FileListLine generate(String text) {
        int[] pathRange = {0, 0};
        int[] hashRange = {0, 0};
        boolean valid = match(text, pathRange, hashRange) &&
                validateMatch(pathRange, hashRange);
        return new FileListLine(text,
                                lineN++,
                                valid,
                                pathRange,
                                hashRange);
    }

    boolean match(String text, int[] pathRange, int[] hashRange) {
        path.reset(text);
        hash.reset(text);
        if (path.find() && hash.find() ) {
            pathRange[0] = path.start();
            pathRange[1] = path.end();

            hashRange[0] = hash.start();
            hashRange[1] = hash.end();

            return true;
        }
        return false;
    }

    boolean validateMatch(int[] pathRange, int[] hashRange) {
        // note regionStart inclusive, regionEnd exclusive
        int[] later = (pathRange[1] > hashRange[1]) ? pathRange : hashRange;
        int[] earlier = (later == pathRange) ? hashRange : pathRange;
        return later[0] >= earlier[1];
    }
}
