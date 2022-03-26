package org.ericghara.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ericghara.argument.Id.EnumKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.ericghara.argument.ArgWithValuesGroupGeneratorTest.TestEnum.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ArgWithValuesGroupGeneratorTest.class,
        ArgumentGroup.class, ArgWithValuesGroupGenerator.class} )
class ArgWithValuesGroupGeneratorTest {

    @AllArgsConstructor
    @Getter
    enum TestEnum implements EnumKey {
        A(null),
        B(List.of() ),
        C(List.of("C") ),
        D(List.of("D", "D") );

        final List<String> options;
    }

    @Autowired
    ArgWithValuesGroupGenerator<EnumKey> generator;

    @Autowired
    ArgumentGroup<EnumKey, ArgDefinition> inGroup;

    @MockBean
    ApplicationArguments appArgs;

    Function<String,Boolean> alwaysTrue = s -> true;


    BiFunction<ArgDefinition, List<String>, ArgWithValues>
            constructor = ArgWithValues::new;


    void setGroup(TestEnum... enums) {
        Arrays.asList(enums).forEach(e ->
                inGroup.add(e,
                        new ArgDefinition(e.name(), alwaysTrue, 0, Integer.MAX_VALUE, Set.of() ) )
        );
    }

    @Test
    void convertNonNullOptions() {
        TestEnum[] enums = {B,C,D};
        setGroup(enums);

        Arrays.asList(enums).forEach( e -> {
            var name = e.name();
            var options = e.options;
            when(appArgs.containsOption(name) ).thenReturn(true);
            when(appArgs.getOptionValues(name) ).thenReturn(options);
        } );


        var outGroup = generator.convert(inGroup, constructor);

        Arrays.asList(enums)
                .forEach( e ->
                        assertIterableEquals(e.options,
                                outGroup.get(e)
                                        .values() ) );
    }

    @Test
    // Special case arg in group but not defined by appArgs
    // This means it was a default option val.  Should return empty list.
    void convertNullOptions() {
        TestEnum[] enums = {A};
        var name = A.name();
        setGroup(enums);
        when(appArgs.containsOption(name ) ).thenReturn(false);

        var outGroup = generator.convert(inGroup, constructor);
        assertIterableEquals(List.of() , outGroup.get(A).values() );
    }

}