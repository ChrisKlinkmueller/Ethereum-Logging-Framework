package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.specification.Constant;
import au.csiro.data61.aap.specification.types.ArrayType;
import au.csiro.data61.aap.specification.types.BoolType;
import au.csiro.data61.aap.specification.types.BytesType;
import au.csiro.data61.aap.specification.types.FixedType;
import au.csiro.data61.aap.specification.types.IntegerType;
import au.csiro.data61.aap.specification.types.StringType;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseStaticValueTest
 */
public class ParseStaticValueTest {
    private final SpecificationParser parser = new SpecificationParser();

    @ParameterizedTest
    @MethodSource("createValidPlainCases")
    void testValidPlainValues(String code, Class<?> typeClass, Object value) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Constant> parserResult = this.parser.parseConstant(is);
        assertTrue(parserResult.isSuccessful(), String.format("Valid value case '%s' failed, due to the following error '%s'", code, parserResult.getErrorMessage()));
        
        final Constant constant = parserResult.getResult();
        assertEquals(typeClass, constant.getType().getClass());
        assertEquals(value, constant.getValue());
    }

    static Stream<Arguments> createValidPlainCases() {
        return Stream.of(
            Arguments.of("0x2131abcfa12389", BytesType.class, "0x2131abcfa12389"),
            Arguments.of("13213458", IntegerType.class, new BigInteger("13213458")),
            Arguments.of("123214.1324324", FixedType.class, new BigDecimal("123214.1324324")),
            Arguments.of("true", BoolType.class, true),
            Arguments.of("false", BoolType.class, false),
            Arguments.of("\"string test\"", StringType.class, "string test")
        );
    }

    @ParameterizedTest
    @MethodSource("createValidArrayCases")
    void testValidArrayValues(String code, Class<?> baseType, Object[] expectedValues) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Constant> parserResult = this.parser.parseConstant(is);
        assertTrue(parserResult.isSuccessful(), String.format("Valid value case '%s' failed, due to the following error '%s'", code, parserResult.getErrorMessage()));
        
        final Constant constant = parserResult.getResult();
        assertEquals(ArrayType.class, constant.getType().getClass());
        assertEquals(baseType, ((ArrayType<?>)constant.getType()).getBaseType().getClass());
        
        @SuppressWarnings("unchecked")
        final List<Object> values = (List<Object>)constant.getValue(); 
        assertEquals(expectedValues.length, values.size(), code);
        
        for (int i = 0; i < values.size(); i++) {
            assertEquals(expectedValues[i], values.get(i));
        }
    }

    static Stream<Arguments> createValidArrayCases() {
        return Stream.of(
            Arguments.of("{0x2131abcfa12389, 0x2314123ad123}", BytesType.class, new Object[]{"0x2131abcfa12389", "0x2314123ad123"}),
            Arguments.of("{13213458,461651416}", IntegerType.class, new Object[]{new BigInteger("13213458"), new BigInteger("461651416")}),
            Arguments.of("{123214.1324324, 1231, 152.56}", FixedType.class, new Object[]{new BigDecimal("123214.1324324"), new BigDecimal("1231.0"), new BigDecimal("152.56")}),
            Arguments.of("{true, false, false, true}", BoolType.class, new Object[]{true, false, false, true}),
            Arguments.of("{\"string test\", \"test string\"}", StringType.class, new Object[]{"string test", "test string"})
        );
    }

    @ParameterizedTest
    @MethodSource("createInvalidStaticValueCases")
    void testInvalidStaticValues(String code) {
        final InputStream is = StringUtil.toStream(code);
        final SpecificationParserResult<Constant> parserResult = this.parser.parseConstant(is);
        assertFalse(parserResult.isSuccessful(), String.format("Invalid value case '%s' failed", code));
    }

    static Stream<Arguments> createInvalidStaticValueCases() {
        return Stream.of(
            Arguments.of("2131abcfa12389h"),
            Arguments.of("{ \"adsa\", 123 }"),
            Arguments.of("{ \"adsa\", "),
            Arguments.of("123.12321.123")
        );
    }
}