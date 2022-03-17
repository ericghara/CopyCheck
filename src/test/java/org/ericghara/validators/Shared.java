package org.ericghara.validators;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class Shared {

    @Nullable
    static <E> List<E> listify(String s, Function<String, E> parser) {
        return Objects.nonNull(s) ?
                Stream.of(s.split("(,)\\s*"))
                        .map(parser)
                        .toList()
                : null;
    }
}
