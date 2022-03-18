package org.ericghara.parser;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.lang.String.format;

@Slf4j
@NoArgsConstructor
public class Matchers {

    @NonNull
    public static Matcher ChildPathsOfSmart(String parentPath) {
        String regEx = String.format("(?<=\\A|\\s)%s([^\\s:]|( )?(?=[\\S]))*", parentPath);
        return Pattern.compile(regEx)
                      .matcher("");
    }

    public static Matcher matchEOL() {
        return Pattern.compile("$")
                    .matcher("");
    }

    public static Matcher probableMD5() {
        // 128 bit digest;
        return matchHex(128);
    }

    public static Matcher probableSHA1() {
        // 160 bit digest;
        return matchHex(160);
    }

    public static Matcher probableSHA256() {
        // 256 bit digest
        return matchHex(256);
    }

    // matches hex beginning a line, following 2 space chars or a tab char;
    static Matcher matchHex(int bits) {
        int BITS_PER_HEX = 4; // = sqrt( (2**4) )
        var hexes = bits/4;
        if (0 != bits % 4) {
            throw new IllegalArgumentException(
                    format("No exact bit to hex conversion for %d", bits) );
        }
        var regex = format("(?<=^( )?|(  )|\\t)[0-9a-fA-F]{%d}\\b", hexes);
        return Pattern.compile(regex)
                      .matcher("");
    }

    @NonNull
    public static Matcher matchLineContainingChildPathOf(String parentPath) {
        final String BolToWhitespacePreceding = "^(.*)(?<=\\s)";
        final String toEol = "%s(.*)$";
        var regEx = BolToWhitespacePreceding + parentPath + toEol;
        return Pattern.compile(regEx)
                      .matcher("");
    }
}
