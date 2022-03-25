package org.ericghara.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ericghara.argument.Id.EnumKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ArgEntryGrouperTest {

    @AllArgsConstructor
    @Getter
    enum TestEnum implements EnumKey {
        A(true),
        B(false),
        C(true),
        D(false),
        E(true);

        final Boolean group;
    }


    Function<EnumArgPair<TestEnum, ArgDefinition>, Boolean>
            groupingFunc = (pair) -> pair.getEnum().group;

    @Test
    void findRequired() {
        var argGroup = new ArgumentGroup<TestEnum, ArgDefinition>();

        Arrays.asList(TestEnum.values() ).forEach( e ->
            argGroup.add(e,
                    new ArgDefinition(e.name(), null, 0, 0, Set.of() ) )
        );

        var argEntryGrouper = new ArgEntryGrouper<>(argGroup, groupingFunc);
        var groups = argEntryGrouper.getResult();
        Arrays.asList(TestEnum.values() )
              .forEach( e -> assertTrue(groups.get(e.group)
                                              .getArgIds()
                                              .contains(e) ) );
    }
}