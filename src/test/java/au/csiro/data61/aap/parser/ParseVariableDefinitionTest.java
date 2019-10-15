package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.specification.types.ArrayType;
import au.csiro.data61.aap.specification.types.BoolType;
import au.csiro.data61.aap.specification.types.FixedType;
import au.csiro.data61.aap.specification.types.IntegerType;
import au.csiro.data61.aap.specification.types.StringType;
import au.csiro.data61.aap.specification.Variable;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseVariableDefinitionTest
 */
public class ParseVariableDefinitionTest {
    private final SpecificationParser parser = new SpecificationParser();

    @ParameterizedTest
    @MethodSource("createValidVariableDefinitions")
    void testValidVariableDefinitions(String input, Variable expectedVariable) {
        final InputStream is = StringUtil.toStream(input);
        final SpecificationParserResult<Variable> result = this.parser.parseVariableDefinition(is);
        assertTrue(result.isSuccessful(), result.getErrorMessage());
    
        final Variable resultVariable = result.getResult();
        assertEquals(resultVariable.getName(), expectedVariable.getName());
        assertEquals(resultVariable.getType(), expectedVariable.getType());
        assertTrue(resultVariable.equals(expectedVariable));
    }

    @ParameterizedTest
    @MethodSource("createInvalidVariableDefinitions")
    void testInvalidVariableDefinitions(String input) {
        final InputStream is = StringUtil.toStream(input);
        final SpecificationParserResult<Variable> result = this.parser.parseVariableDefinition(is);
        assertFalse(result.isSuccessful());
    }

    private static Stream<Arguments> createValidVariableDefinitions() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("string test", new Variable(new StringType(), "test")),
            Arguments.of("uint8[] test", new Variable(new ArrayType<>(new IntegerType(false, 8)), "test")),
            Arguments.of("fixed8x29 test", new Variable(new FixedType(true, 8, 29), "test")),
            Arguments.of("string test.test", new Variable(new StringType(), "test.test")),
            Arguments.of("bool test:test", new Variable(new BoolType(), "test:test"))
        );
        return stream;
    }  

    private static Stream<Arguments> createInvalidVariableDefinitions() {
        return Stream.of(
            Arguments.of("string2 test"),
            Arguments.of("fixed23x8 testor"),
            Arguments.of("fixed8x80"),
            Arguments.of("string test?test")
        );
    }  
}