package au.csiro.data61.aap.elf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import au.csiro.data61.aap.samples.SampleUtils;

public class ValidatorSemanticSpec {
    private final Validator validator = new Validator();    

    private void testProcessing(String script, List<String> errorMessages) {
        try {
            final List<EthqlProcessingEvent> errors = validate(script);
            assertEquals(errorMessages.size(), errors.size());
            for (int i = 0; i < errors.size(); i++) {
                assertEquals(errors.get(i).getErrorMessage(), errorMessages.get(i));
            }
        } catch (EthqlProcessingException e) {
            final String message = String.format(
                "Processing of script '%s' should not have resulted in exception, but in error messages '%s'.",
                script,
                errorMessages.stream().collect(Collectors.joining(", "))
            );
            fail(message, e);
        }
    }

    private List<EthqlProcessingEvent> validate(String script) throws EthqlProcessingException {
        return this.validator.analyzeScript(new ByteArrayInputStream(script.getBytes()));
    }

    @ParameterizedTest
    @MethodSource("sampleScripts")
    public void processSampleScript(URL url) {
        try {
            final List<EthqlProcessingEvent> errors = this.validator.analyzeScript(url.getFile());
            assertEquals(0, errors.size());
        } catch (EthqlProcessingException ex) {
            fail(ex);
        }
    }

    private static Stream<Arguments> sampleScripts() {
        return SampleUtils.getAllResources().stream().map(Arguments::of);
    }

    @ParameterizedTest
    @ValueSource(strings = { "notExist.ethql", "D:/test_1231243/error.ethql", "C:/" })
    public void processNonExistingScript(String file) {
        try {
            this.validator.analyzeScript(file);
            fail();
        } catch (EthqlProcessingException ex) {
            final String message = String.format("Invalid file path: '%s'.", file);
            assertEquals(message, ex.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("variableDefinitionScripts")
    public void processVariableDefinition(String script, List<String> errorMessages) {
        this.testProcessing(script, errorMessages);
    }

    private static Stream<Arguments> variableDefinitionScripts() {
        return Stream.of(
            Arguments.of("string a = \"\"; string a = \"\";", List.of("Variable 'a' is already defined.")),
            Arguments.of("b = 1;", List.of("Variable 'b' not defined."))
        );
    }

    @ParameterizedTest
    @MethodSource("methodCallScripts")
    public void processMethodCall(String script, List<String> errorMessages) {
        this.testProcessing(script, errorMessages);
    }

    private static Stream<Arguments> methodCallScripts() {
        return Stream.of(
            Arguments.of("connect(\"localhost:8465\");", Collections.emptyList()),
            Arguments.of("int result = add(10, -5);", Collections.emptyList()),
            Arguments.of("connec(\"localhost:8465\");", List.of("Method 'connec' with parameters 'string' unknown.")),
            Arguments.of("connect(\"localhost:8465\", 5);", List.of("Method 'connect' with parameters 'string, int' unknown.")),
            Arguments.of("connect(5);", List.of("Method 'connect' with parameters 'int' unknown.")),
            Arguments.of(
                "string result = contains({0x931D387731bBbC988B312206c74F77D004D6B84c}, 0x931D387731bBbC988B312206c74F77D004D6B84c);",
                List.of("Cannot assign a bool value to a string variable.")
            ),
            Arguments.of(
                "int result = mapValue(4, \"unknown\", {0,1,2,3}, {\"first\", \"second\", \"third\", \"fourth\"});",
                List.of("Cannot assign a string value to a int variable.")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("emitScripts")
    public void processEmitScripts(String script, List<String> errorMessages) {
        this.testProcessing(script, errorMessages);
    }

    private static Stream<Arguments> emitScripts() {
        return Stream.of(
            Arguments.of(
                "EMIT LOG LINE (\"Block\", 5, \": New FeeToken registered with address '\", 0x123, \"'.\");",
                Collections.emptyList()
            ),
            Arguments.of("EMIT CSV ROW (\"table\") (5 AS blockNumber, 0x123 AS addr); ", Collections.emptyList()),
            Arguments.of("int kittyId = 15; EMIT XES EVENT ()(kittyId)()(\"birth\" AS xs:string concept:name);", Collections.emptyList()),
            Arguments.of(
                "EMIT LOG LINE (\"Block \", block.number, \": New FeeToken registered with address '\", 0x123, \"'.\");",
                List.of("Variable 'block.number' not defined.")
            ),
            Arguments.of("EMIT CSV ROW (tableName) (5 AS blockNumber);", List.of("Variable 'tableName' not defined.")),
            Arguments.of("EMIT CSV ROW (\"table\") (5);", List.of("Attribute name must be specified for literals")),
            Arguments.of("EMIT XES EVENT()(catId)()(\"birth\" AS xs:string concept:name);", List.of("Variable 'catId' not defined.")),
            Arguments.of(
                "int kittyId = 15; EMIT XES EVENT()(kittyId)()(birth AS xs:string concept:name);",
                List.of("Variable 'birth' not defined.")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("blockFilterScripts")
    public void processBlockFilterScripts(String script, List<String> errorMessages) {
        this.testProcessing(script, errorMessages);
    }

    private static Stream<Arguments> blockFilterScripts() {
        return Stream.of(
            Arguments.of("BLOCKS (6605100) (6615100) { TRANSACTIONS (ANY) (ANY) {} }", Collections.emptyList()),
            Arguments.of("BLOCKS (Earliest) (cuRRent) { TRANSACTIONS (ANY) (ANY) {} }", Collections.emptyList()),
            Arguments.of(
                "BLOCKS (var1) (8) {}",
                List.of(
                    "Variable 'var1' not defined.",
                    "The 'from' block number must be an integer variable, an integer literal or one of the values {EARLIEST, CURRENT}."
                )
            ),
            Arguments.of("BLOCKS (10) (8) {}", List.of("The 'from' block number must be smaller than or equal to the 'to' block number.")),
            Arguments.of("BLOCKS (-10) (8) {}", List.of("The 'from' block number must be an integer larger than or equal to 0.")),
            Arguments.of(
                "BLOCKS (10) (-8) {}",
                List.of(
                    "The 'to' block number must be an integer larger than or equal to 0.",
                    "The 'from' block number must be smaller than or equal to the 'to' block number."
                )
            ),
            Arguments.of(
                "BLOCKS (\"123\") (10) {}",
                List.of("The 'from' block number must be an integer variable, an integer literal or one of the values {EARLIEST, CURRENT}.")
            ),
            Arguments.of(
                "BLOCKS (123) (0x123) {}",
                List.of("The 'to' block number must be an integer variable, an integer literal or one of the values {CONTINUOUS, CURRENT}.")
            ),
            Arguments.of(
                "TRANSACTIONS (ANY) (ANY) { BLOCKS (0) (1) {} }",
                List.of("Invalid nesting of filters.", "Invalid nesting of filters.")
            )
        );
    }
}
