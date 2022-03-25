package org.ericghara.validators;

import org.ericghara.argument.ArgDefinition;
import org.ericghara.argument.ArgumentGroup;
import org.ericghara.argument.FoundArgs;
import org.ericghara.argument.Id.AppArg;
import org.ericghara.argument.SingleValueArgument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.List;

import static org.ericghara.validators.Shared.listify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes={ArgumentValidatorTest.class})
class ArgumentValidatorTest {

    ArgumentValidator<AppArg, ArgDefinition, SingleValueArgument> argValidator;

    @MockBean
    ApplicationArguments appArguments;

    @MockBean
    FoundArgs<AppArg, ArgDefinition, SingleValueArgument> foundArgs;

    @MockBean
    ArgumentGroup<AppArg, ArgDefinition> argumentDefGroup;

    @Mock
    ArgumentGroup<AppArg, SingleValueArgument> argumentValGroup;

    @BeforeEach
    void before() {
        argValidator = new ArgumentValidator<>();
    }

    @ParameterizedTest(name="[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
            Label                      | recognized | required | all | expected
            "unrecognized arg name"    |    false   |    true   |   true    | false
            "Improper required arg(s)" |    true    |    false  |   true    | false
            "Improper optional arg(s)" |    true    |    true   |   false   | false
            "everything OK"            |    true    |    true   |   true    | true
            """)
    void isValid(String _label, boolean recognized, boolean required, boolean all, boolean expected) {
        when(foundArgs.getAllDefined() ).thenReturn(null);
        ArgumentValidator<AppArg, ArgDefinition, SingleValueArgument> spy
                = Mockito.spy(argValidator);
        doReturn(recognized).when(spy)
                            .allRecognized(any(), any(ApplicationArguments.class) );
        doReturn(required).when(spy)
                          .hasRequired(any() );
        doReturn(all).when(spy)
                        .validateAll(any() );
        assertEquals(expected, spy.isValid(foundArgs, appArguments) );
    }

    @ParameterizedTest(name="[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
            Label            |    optionVals   | Validator Responses  | expected
            "invalid value"  |  A, B, C   |    true, false, true | false
            "all valid"      |  A, B, C   |    true, true, true  | true
            "No args"        |     ,      |           ,          | true
            """)
    void validateGroup(String _label, String optionValStr, String validatorResStr, boolean expected) {
        List<Boolean> validatorRes = listify(validatorResStr, Boolean::parseBoolean);
        List<List<String>> optionVals = listify(optionValStr, List::of);
        int numArgs = optionVals.size();
        var args = new HashSet<SingleValueArgument>();

        for (int i = 0; i < numArgs; i++) {
            var m = mock(SingleValueArgument.class);
            when(m.validate(any() ) ).thenReturn(validatorRes.get(i) );
            when(m.values() ).thenAnswer(new ReturnsElementsOf(optionVals) );
            args.add(m);
        }

        when(argumentValGroup.getArgs() ).thenReturn(args);
        assertEquals(expected, argValidator.validateGroup(argumentValGroup) );
    }

    @ParameterizedTest(name="[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
            # Any value greater than 0 indicates required
            Label         | inputNames | validNames |  expected
            "All Valid"   | A, B     | A, B, C  | true
            "One Invalid" | A, B, C  | A, B     | false
            "Empty input" |    ,     | A, B, C  | true
            """)
    void allRecognized(String _label, String inputNames, String validNames, boolean expected) {
        when(appArguments.getOptionNames() )
                .thenReturn(new HashSet<>(listify(inputNames,(s) -> s) ) );
        when(argumentDefGroup.getNames() )
                .thenReturn(new HashSet<>(listify(validNames,(s) -> s) ) );
        assertEquals(expected, argValidator.allRecognized(argumentDefGroup, appArguments) );
    }
}