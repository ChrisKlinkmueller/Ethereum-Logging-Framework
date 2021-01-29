package blf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Stream;

class ValidatorSemanticTest {
    static final private Validator validator = new Validator();

    static List<BcqlProcessingError> validate(String script) {
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
        return YamlTestLoader.readTestData("ValidatorSemanticTestTransactionData.yaml", ValidatorSemanticTest.class.getClassLoader());
    }

    @ParameterizedTest(name = "script: {0} expected error msg: {1}")
    @MethodSource("genericProvider")
    void genericFilter(String script, String expectedErrorMsg) {
        List<BcqlProcessingError> errors = validate(script);

        errors.forEach(err -> Assertions.assertEquals(err.getErrorMessage(), expectedErrorMsg));
    }

    static Stream<Arguments> genericProvider() {
        return YamlTestLoader.readTestData("ValidatorSemanticTestGenericData.yaml", ValidatorSemanticTest.class.getClassLoader());
    }

    @ParameterizedTest(name = "script: {0} expected error msg: {1}")
    @MethodSource("logEntryProvider")
    void logEntryFilter(String script, String expectedErrorMsg) {
        List<BcqlProcessingError> errors = validate(script);

        errors.forEach(err -> Assertions.assertEquals(err.getErrorMessage(), expectedErrorMsg));
    }

    static Stream<Arguments> logEntryProvider() {
        return YamlTestLoader.readTestData("ValidatorSemanticTestLogEntryData.yaml", ValidatorSemanticTest.class.getClassLoader());
    }
}
