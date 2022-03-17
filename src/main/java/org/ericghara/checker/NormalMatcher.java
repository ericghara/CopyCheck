package org.ericghara.checker;

public class NormalMatcher {
    
    public static boolean isMatch(String expected, String found) {
        return expected.equalsIgnoreCase(found);
    }
}
