package org.ericghara.validators;

import org.ericghara.argument.ArgDefinition;
import org.ericghara.utils.FileSystemUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes={FileSystemUtils.class})
class ValueValidatorTest {

    @MockBean
    FileSystemUtils fsUtils;

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
        List<String> valueList = Shared.listify(values, String::toString);
        List<Boolean> validatorResList = Shared.listify(validatorFuncRes, Boolean::parseBoolean);

        when(fsUtils.isDir(any(String.class)))
                .thenAnswer(new ReturnsElementsOf(validatorResList));
        var valueValidator = new ArgDefinition("unitTest", fsUtils::isDir, minValues, maxValues);
        assertEquals(expected, valueValidator.validate(valueList));
    }

}