package org.ericghara.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ericghara.argument.Id.EnumKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.ericghara.argument.FoundArgsTest.FoundArgsTestEnum.*;
import static org.ericghara.argument.Id.ArgGroupKey.DEFAULT;
import static org.ericghara.argument.Id.ArgGroupKey.REQUIRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes={FoundArgsTest.class, ArgWithValuesGroupGenerator.class})
class FoundArgsTest {

    @AllArgsConstructor
    @Getter
    enum FoundArgsTestEnum implements EnumKey {
        A,
        B,
        C,
        D
    }

    @MockBean
    ApplicationArguments appArgs;

    @Autowired
    ArgWithValuesGroupGenerator<EnumKey> generator;

    ArgumentGroup<EnumKey, ArgDefinition> all;

    FoundArgs<EnumKey, ArgDefinition, ArgWithValues> foundArgs;

    Function<String,Boolean> alwaysTrue = s -> true;

    BiFunction<ArgDefinition, List<String>, ArgWithValues>
            constructor = ArgWithValues::new;

    void setAll(Set<FoundArgsTestEnum> defaults, Set<FoundArgsTestEnum> nonDefaults) {
        all = new ArgumentGroup<>();

        defaults.forEach(e ->
                all.add( e, new ArgDefinition(e.name(), alwaysTrue, 0, Integer.MAX_VALUE, Set.of(DEFAULT, REQUIRED) ) )
        );
        nonDefaults.forEach( e ->
                all.add( e, new ArgDefinition(e.name(), alwaysTrue, 0, Integer.MAX_VALUE, Set.of(REQUIRED) ) )
        );
    }

    void addAppArg(FoundArgsTestEnum arg) {
        var name = arg.name();
        when(appArgs.containsOption(name) ).thenReturn(true);
        when(appArgs.getOptionValues(name) ).thenReturn(List.of() );
    }

    @Test
    void getAll() {
        var defaults = Set.of(A);
        var nonDefaults = Set.of(B,C,D);
        setAll(defaults, nonDefaults);

        foundArgs = new FoundArgs<>(all, appArgs, generator, constructor);
        assertEquals(defaults, foundArgs.getFound(REQUIRED).getArgIds() );
    }

    @Test
    void getFoundFindsDefaults() {
        var defaults = Set.of(A);
        var nonDefaults = Set.of(B,C,D);
        setAll(defaults, nonDefaults);

        foundArgs = new FoundArgs<>(all, appArgs, generator, constructor);
        assertEquals(Set.of(FoundArgsTestEnum.values() ), foundArgs.getAll().getArgIds() );
    }

    @Test
    void getFoundFindsArgs() {
        addAppArg(B);
        addAppArg(D);
        var defaults = Set.of(A);
        var nonDefaults = Set.of(B,C,D);
        setAll(defaults, nonDefaults);

        foundArgs = new FoundArgs<>(all, appArgs, generator, constructor);
        assertEquals(foundArgs.getFound(REQUIRED).getArgIds(), Set.of(B,D) );
    }

    @Test
    void partitionBy() {
        var defaults = Set.of(A);
        var nonDefaults = Set.of(B);
        setAll(defaults, nonDefaults);

        foundArgs = new FoundArgs<>(all, appArgs, generator, constructor);
        var filters = new HashSet<>(foundArgs.getGroupFilters() );
        filters.remove(REQUIRED);
        assertEquals( defaults, foundArgs.getFound(REQUIRED).getArgIds() );
        filters.forEach( g ->
                assertThrows(NoSuchElementException.class, () -> foundArgs.getFound(g) )
        );
    }
}