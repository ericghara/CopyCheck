package org.ericghara.validators;

import org.ericghara.argument.ArgDefinition;
import org.ericghara.configs.ValidatorConfig;
import org.ericghara.utils.FileSystemUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.ericghara.validators.Shared.listify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = {ValidatorConfig.class,ArgumentValidator.class, FileSystemUtils.class})
class ArgumentValidatorTest {

    @Autowired
    ArgumentValidator argValidator;

    @MockBean
    ApplicationArguments appArguments;


    @ParameterizedTest(name="[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
            Label                      | recognized | validate0 | validate1 | expected
            "unrecognized arg name"    |    false   |    true   |   true    | false
            "Improper required arg(s)" |    true    |    false  |   true    | false
            "Improper optional arg(s)" |    true    |    true   |   false   | false
            "everything OK"            |    true    |    true   |   true    | true
            """)
    void isValid(String _label, boolean recognized, boolean validate0, boolean validate1, boolean expected) {
        ArgumentValidator spy = Mockito.spy(argValidator);
        doReturn(recognized).when(spy)
                            .allRecognized(any(ApplicationArguments.class) );
        doReturn(validate0).doReturn(validate1)
                           .when(spy)
                           .validateGroup(ArgumentMatchers.any(),
                                   any(ApplicationArguments.class));
        assertEquals(expected, spy.isValid(appArguments) );
    }

    @ParameterizedTest(name="[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
            Label            |    Names   | Validator Responses  | expected
            "invalid value"  |  A, B, C   |    true, false, true | false
            "all valid"      |  A, B, C   |    true, true, true  | true
            "No args"        |     ,      |           ,          | true
            """)
    void validate(String _label, String argNameStr, String validatorResStr, boolean expected) {
        List<String> argNames = listify(argNameStr, (s) -> s);
        List<Boolean> validatorRes = listify(validatorResStr, Boolean::parseBoolean);
        when(appArguments.getOptionNames() )
                .thenReturn(new HashSet<String>(argNames) );
        var argValidator = new ArgumentValidator(
                optionsMockValidator(argNames, validatorRes), appArguments);
        assertEquals(expected,
                argValidator.validateGroup(argNames, appArguments) );
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
                .thenReturn( new HashSet<>(listify(inputNames,(s) -> s)  ) );
        List<String> valid = listify(validNames, (s) -> s );
        List<Integer> required = valid.stream()
                                      .map( (s) -> 1 )
                                      .toList();
        var options = optionsMockRequired(valid, required);
        var argumentValidator = new ArgumentValidator(options, appArguments);
        assertEquals(expected, argumentValidator.allRecognized(appArguments) );
    }

    @ParameterizedTest(name="[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
            # Any value greater than 0 indicates required
            Label                      | valNames | required |  expected
            "B, C of A, B, C required" | A, B, C  | 0, 1, 1  |   B, C
            "None of A, B, C required" | A, B, C  | 0, 0, 0  | ,
            """)
    void getRequired(String _label, String valNames, String required, String expectedStr) {
        List<String> names = listify(valNames, String::toString);
        List<Integer> requiredList = listify(required, Integer::parseInt);
        List<String> expected = listify(expectedStr, String::toString);

        var argValidator = new ArgumentValidator(optionsMockRequired(names, requiredList), appArguments);
        assertIterableEquals(expected, argValidator.getRequired() );
    }


    Map<String, ArgDefinition> optionsMockRequired(List<String> names, List<Integer> requiredList) {
        Map<String, ArgDefinition> options = new HashMap<>();

        for (int i = 0; i < names.size(); i++) {
            var valValidator = mock(ArgDefinition.class);
            var n = names.get(i);
            var m = requiredList.get(i);
            when(valValidator.name()).thenReturn(n);
            when(valValidator.minOptions()).thenReturn(m);
            options.put(n, valValidator);
        }
        return options;
    }

    Map<String, ArgDefinition> optionsMockValidator(List<String> names, List<Boolean> validatorRes) {
        Map<String, ArgDefinition> options = new HashMap<>();

        for (int i = 0; i < names.size(); i++) {
            var valValidator = mock(ArgDefinition.class);
            var n = names.get(i);
            var r = validatorRes.get(i);
            when(valValidator.name()).thenReturn(n);
            when(valValidator.validate(ArgumentMatchers.any() )).thenReturn(r);
            options.put(n, valValidator);
        }
        return options;
    }


}