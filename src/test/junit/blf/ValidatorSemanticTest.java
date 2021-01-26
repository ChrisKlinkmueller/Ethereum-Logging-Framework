package blf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class ValidatorSemanticTest {
    static final private Validator validator = new Validator();

    static List<BcqlProcessingError> validate(String script)  {
        try {
            return validator.analyzeScript(new ByteArrayInputStream(script.getBytes()));
        } catch (BcqlProcessingException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @ParameterizedTest(name = "script: {0} expected error msg: {1}")
    @MethodSource("transactionProvider")
    void transactionFilter(String script, String expectedErrorMsg) {
        List<BcqlProcessingError> errors = validate(script);

        errors.forEach(err -> Assertions.assertEquals(err.getErrorMessage(), expectedErrorMsg));
    }

    static Stream<Arguments> transactionProvider() {
        return Stream.of(
                Arguments.of(
                        "SET BLOCKCHAIN \"Ethereum\"\n" +
                        "BLOCKS (1) (5) {\n" +
                        "TRANSACTIONS (ANY) (\n" +
                        "0x931D387731bBbC988B312206c74F77D004D6B84b,\n" +
                        "0x931D387731bBbC988B312206c74F77D004D6B84c\n" +
                        ") {}\n" +
                        "}\n",
                        "")
//                Arguments.of("", ""),
//                Arguments.of("", "Invalid nesting of filters."),
//                Arguments.of("", "'0x123' is not a valid address literal.")
        );
    }

//    @ParameterizedTest
//    @ValueSource
//    void genericFilter() {
//
//    }

//    @ParameterizedTest
//    @ValueSource
//    void logEntriesFilter() {
//
//    }
}
