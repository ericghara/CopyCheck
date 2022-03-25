package org.ericghara.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ericghara.argument.Id.EnumKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    static ArgumentGroup<TestEnum, ArgDefinition> argGroup;

    Function<EnumArgPair<TestEnum, ArgDefinition>, Boolean>
            groupingFunc = (pair) -> pair.getEnum().group;

    Function<EnumArgPair<TestEnum, ArgDefinition>, String>
            nameGroupingFunc = (pair) -> pair.getEnum().name();

    @BeforeAll
    static void before() {
        argGroup = new ArgumentGroup<TestEnum, ArgDefinition>();

        Arrays.asList(TestEnum.values() ).forEach( e ->
                argGroup.add(e,
                        new ArgDefinition(e.name(), null, 0, 0, Set.of() ) )
        );
    }

    @Test
    void findRequiredMultiMemberGroups() {
        var argEntryGrouper = new ArgEntryGrouper<>(argGroup, groupingFunc);
        var groups = argEntryGrouper.getResult();
        Arrays.asList(TestEnum.values() )
              .forEach( e -> assertTrue(groups.get(e.group)
                                              .getArgIds()
                                              .contains(e) ) );
    }

    @Test
    void findRequiredSingleMemberGroups() {
        var argEntryGrouper = new ArgEntryGrouper<>(argGroup, nameGroupingFunc);
        var groups = argEntryGrouper.getResult();
        Arrays.asList(TestEnum.values() )
                .forEach( e -> assertTrue(groups.get(e.name() )
                        .getArgIds()
                        .contains(e) ) );
        groups.values().forEach( g -> assertEquals(1, g.size() ) );
    }
}
