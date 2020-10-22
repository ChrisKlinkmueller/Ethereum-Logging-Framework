package au.csiro.data61.aap.elf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import au.csiro.data61.aap.samples.SampleUtils;

public class ValidatorSemanticSpec {
    private final Validator validator = new Validator();

    @ParameterizedTest
    @MethodSource("sampleScripts")
    public void validateSampleScript(URL url) {
        try {
            final List<EthqlProcessingError> errors = this.validator.analyzeScript(url.getFile());
            assertEquals(0, errors.size());
        } catch (EthqlProcessingException ex) {
            fail(ex);
        }
    }

    private static Stream<Arguments> sampleScripts() {
        return SampleUtils.getAllResources().stream().map(Arguments::of);
    }

    @ParameterizedTest
    @ValueSource(strings = { "notExist.ethql" })
    public void validateNonExistingScript(String file) {
        try {
            this.validator.analyzeScript(file);
            fail();
        } catch (EthqlProcessingException ex) {
            final String message = String.format("Invalid file path: '%s'.", file);
            assertEquals(message, ex.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("invalidVariableDefinitionScripts")
    public void validateInvalidVariableDefinitionScripts(String script, String errorMessage) {
        try {
            final List<EthqlProcessingError> errors = validate(script);
            assertEquals(1, errors.size());
            assertEquals(errors.get(0).getErrorMessage(), errorMessage);
        } catch (EthqlProcessingException e) {
            final String message = String.format("Processing of script '%s' should not have resulted in exception, but in error message '%s'.", script, errorMessage);
            fail(message, e);            
        }

    }

    private static Stream<Arguments> invalidVariableDefinitionScripts() {
        return Stream.of(
            Arguments.of("string a = \"\"; string a = \"\";", "Variable 'a' is already defined."),
            Arguments.of("b = 1;", "Variable 'b' not defined.")
        );
    }

    private List<EthqlProcessingError> validate(String script) throws EthqlProcessingException {
        return this.validator.analyzeScript(new ByteArrayInputStream(script.getBytes()));
    }
}
