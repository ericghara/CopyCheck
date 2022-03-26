package org.ericghara.validators;

import org.ericghara.argument.ArgDefinition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ValueValidatorTest {

    @Mock
    Function<String,Boolean> validatorFunc;


    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
                   TEST LABEL           |  VALUES  | VALIDATOR_FUNC_RESPONSES | MIN_NUM_VALUES | MAX_NUM_VALUES | EXPECTED
             "Num values = max num"     | A, B, C  | true, true, true         |        1       |       3        |    true
             "Num values = min num"     |    A     | true                     |        1       |       3        |    true
             "Num values = min num = 0" |     ,    |        ,                 |        0       |       3        |    true
             "Num values > max"         |  A,B,C,D | true, true, true, true   |        1       |       3        |    false
             "Num Values < min"         |     ,    |         ,                |        1       |       3        |    false
             "Validator func false"     |  A,B,C   |   true, true, false      |        1       |       3        |    false
             "null value"               |          |   false                  |        0       |       3        |    false
            """)
    void validate(String _label, String values, String validatorFuncRes,
                  int minValues, int maxValues, boolean expected) {
        List<String> valueList =
                Shared.listify(values, String::toString);
        List<Boolean> validatorResList =
                Shared.listify(validatorFuncRes, Boolean::parseBoolean);
        lenient().when(validatorFunc
                 .apply(any(String.class)))
                 .thenAnswer(new ReturnsElementsOf(validatorResList));
        var valueValidator =
                new ArgDefinition("unitTest", validatorFunc, minValues, maxValues, Set.of() );
        assertEquals(expected,
                valueValidator.validate(valueList));
    }

}