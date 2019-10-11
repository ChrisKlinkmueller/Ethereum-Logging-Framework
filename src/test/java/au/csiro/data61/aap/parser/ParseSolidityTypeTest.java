package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.specification.types.AddressType;
import au.csiro.data61.aap.specification.types.ArrayType;
import au.csiro.data61.aap.specification.types.BoolType;
import au.csiro.data61.aap.specification.types.BytesType;
import au.csiro.data61.aap.specification.types.FixedType;
import au.csiro.data61.aap.specification.types.IntegerType;
import au.csiro.data61.aap.specification.types.SolidityType;
import au.csiro.data61.aap.specification.types.StringType;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseSolidityTypeTest
 */
public class ParseSolidityTypeTest {
    private final SpecificationParser parser = new SpecificationParser();
    
    @ParameterizedTest
    @MethodSource("createValidNonconfigurableTypes")
    void testValidNonconfigurableTypes(String keyword, String className) {
        getType(keyword, true, className);
    }

    private static Stream<Arguments> createValidNonconfigurableTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("string", StringType.class.getName()),
            Arguments.of("bool", BoolType.class.getName()),
            Arguments.of("address", AddressType.class.getName())
        );
        return stream;
    }  
    
    @ParameterizedTest
    @MethodSource("createValidNonconfigurableArrayTypes")
    void testValidNonconfigurableArrayTypes(String keyword, String className) {
        final ArrayType<?> type = (ArrayType<?>)getType(keyword, true, ArrayType.class.getName());
        assertEquals(type.getBaseType().getClass().getName(), className);
    }

    private static Stream<Arguments> createValidNonconfigurableArrayTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("bool[]", BoolType.class.getName()),
            Arguments.of("address[]", AddressType.class.getName())
        );
        return stream;
    }  

    @ParameterizedTest
    @MethodSource("createValidBytesTypes")
    void testValidBytesTypes(String keyword, boolean isDynamic, int length) {
        final BytesType type = (BytesType)getType(keyword, true, BytesType.class.getName());
        testBytesType(type, isDynamic, length);
    } 

    private static Stream<Arguments> createValidBytesTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("bytes", true, 0),
            Arguments.of("byte", false, 1),
            Arguments.of("bytes1", false, 1),
            Arguments.of("bytes32", false, 32),
            Arguments.of("bytes27", false, 27)
        );
        return stream;
    }  

    @ParameterizedTest
    @MethodSource("createValidBytesArrayTypes")
    void testValidBytesArrayTypes(String keyword, boolean isDynamic, int length) {
        final ArrayType<?> type = (ArrayType<?>)getType(keyword, true, ArrayType.class.getName());
        final BytesType baseType = (BytesType)type.getBaseType();
        testBytesType(baseType, isDynamic, length);
    }

    private static Stream<Arguments> createValidBytesArrayTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("byte[]", false, 1),
            Arguments.of("bytes1[]", false, 1),
            Arguments.of("bytes32[]", false, 32),
            Arguments.of("bytes27[]", false, 27)
        );
        return stream;
    }  

    private void testBytesType(BytesType type, boolean isDynamic, int length) {
        assertTrue(isDynamic == type.isDynamic());
        if (!isDynamic) {
            assertEquals(length, type.getBytesLength());;
        }
    }

    @ParameterizedTest
    @MethodSource("createValidIntegerTypes")
    void testValidIntegerTypes(String keyword, boolean unsigned, int length) {
        final IntegerType type = (IntegerType)getType(keyword, true, IntegerType.class.getName());
        this.testIntegerType(type, unsigned, length);
    }

    private static Stream<Arguments> createValidIntegerTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("uint", true, 256),
            Arguments.of("int", false, 256),
            Arguments.of("uint80", true, 80),
            Arguments.of("int80", false, 80),
            Arguments.of("uint8", true, 8),
            Arguments.of("int256", false, 256),
            Arguments.of("uint112", true, 112),
            Arguments.of("int232", false, 232)
        );
        return stream;
    }  

    @ParameterizedTest
    @MethodSource("createValidIntegerArrayTypes")
    void testValidIntegerArrayTypes(String keyword, boolean unsigned, int length) {
        final ArrayType<?> type = (ArrayType<?>)getType(keyword, true, ArrayType.class.getName());
        assertEquals(type.getBaseType().getClass(), IntegerType.class);
        this.testIntegerType((IntegerType)type.getBaseType(), unsigned, length);
    }

    private static Stream<Arguments> createValidIntegerArrayTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("uint[]", true, 256),
            Arguments.of("int[]", false, 256),
            Arguments.of("uint80[]", true, 80),
            Arguments.of("int80[]", false, 80),
            Arguments.of("uint8[]", true, 8),
            Arguments.of("int256[]", false, 256),
            Arguments.of("uint112[]", true, 112),
            Arguments.of("int232[]", false, 232)
        );
        return stream;
    }  

    private void testIntegerType(IntegerType type, boolean unsigned, int length) {
        assertTrue(type.isUnsigned() == unsigned);
        assertEquals(type.getLength(), length);
    }

    @ParameterizedTest
    @MethodSource("createValidFixedTypes")
    void testValidFixedType(String keyword, boolean unsigned, int m, int n) {
        final FixedType type = (FixedType)getType(keyword, true, FixedType.class.getName());
        this.testFixedType(type, unsigned, m, n);
    }

    private static Stream<Arguments> createValidFixedTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("ufixed", true, 128, 18),
            Arguments.of("fixed", false, 128, 18),
            Arguments.of("ufixed256x80", true, 256, 80),
            Arguments.of("fixed8x0", false, 8, 0),
            Arguments.of("ufixed72x56", true, 72, 56)
        );
        return stream;
    }  

    @ParameterizedTest
    @MethodSource("createValidFixedArrayTypes")
    void testValidFixedArrayType(String keyword, boolean unsigned, int m, int n) {
        final ArrayType<?> type = (ArrayType<?>)getType(keyword, true, ArrayType.class.getName());
        assertEquals(FixedType.class, type.getBaseType().getClass());
        this.testFixedType((FixedType)type.getBaseType(), unsigned, m, n);
    }

    private static Stream<Arguments> createValidFixedArrayTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("ufixed[]", true, 128, 18),
            Arguments.of("fixed[]", false, 128, 18),
            Arguments.of("ufixed256x80[]", true, 256, 80),
            Arguments.of("fixed8x0[]", false, 8, 0),
            Arguments.of("ufixed72x56[]", true, 72, 56)
        );
        return stream;
    } 

    private void testFixedType(FixedType type, boolean unsigned, int m, int n) {
        assertTrue(type.isUnsigned() == unsigned);
        assertEquals(type.getM(), m);
        assertEquals(type.getN(), n);
    }



    @ParameterizedTest
    @MethodSource("createInvalidTypes")
    void testInvalidTypes(String keyword) {
        getType(keyword, false, null);
    }

    private static Stream<Arguments> createInvalidTypes() {        
        Stream<Arguments> stream = Stream.of(
            Arguments.of("ufixed152"),
            Arguments.of("uads"),
            Arguments.of("case"),
            Arguments.of("ufixed7x19"),
            Arguments.of("ufixed8x81"),
            Arguments.of("ufixed256x-1"),
            Arguments.of("int255"),
            Arguments.of("int243"),
            Arguments.of("bytes0"),
            Arguments.of("bytes33"),
            Arguments.of("unfixed["),
            Arguments.of("int]"),
            Arguments.of("bytes[]"),
            Arguments.of("string[]")
        );
        return stream;
    }  

    SolidityType<?> getType(String keyword, boolean isValidType, String className) {
        final InputStream is = StringUtil.toStream(keyword);
        final SpecificationParserResult<SolidityType<?>> result = parser.parseSolidityType(is);
        assertTrue(result.isSuccessful() == isValidType, String.format("Test case '%s' develivered result '%s'", keyword, result.getResult()));
        
        if (isValidType) {
            assertTrue(result.getResult().getClass().getName().equals(className));
            return result.getResult();
        }

        return null;
    }

    
}