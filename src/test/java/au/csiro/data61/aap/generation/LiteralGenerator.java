package au.csiro.data61.aap.generation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.VariableCategory;
import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityString;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * LiteralGenerator
 */
public class LiteralGenerator {
    private static final String DIGITS_WITHOUT_ZERO = "123456789";
    private static final String DIGITS_WITH_ZERO = "0" + DIGITS_WITHOUT_ZERO;
    private static final String HEX_DIGITS = DIGITS_WITH_ZERO + "abcdefABCDEF";
    private static final String STRING_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.?<>/\\\n:;\"'{}[]!@#$%^&*()_-+='";
    
    private static final int ADDRESS_LENGTH = 20;
    private static final int MIN_BYTES_LENGTH = 1;
    private static final int MAX_BYTES_LENGTH = 32;
    private static final int MAX_INT_LENGTH = 18;
    private static final int MAX_STRING_LENGTH = 100;
    private static final int MIN_LIST_LENGTH = 1;
    private static final int MAX_LIST_LENGTH = 10;

    private int count;    
    private final Random random;
    private final Map<Class<? extends SolidityType>, Function<SolidityType, Object>> literalCreators; 
    
    public LiteralGenerator(Random random) {
        assert random != null;
        this.random = random;
        this.count = 0;
        this.literalCreators = new HashMap<>();
        this.literalCreators.put(SolidityAddress.class, this::generateAddressValue);
        this.literalCreators.put(SolidityArray.class, this::generateArrayValue);
        this.literalCreators.put(SolidityBool.class, this::generateBoolValue);
        this.literalCreators.put(SolidityBytes.class, this::generateBytesValue);
        this.literalCreators.put(SolidityFixed.class, this::generateFixedValue);
        this.literalCreators.put(SolidityInteger.class, this::generateIntegerValue);
        this.literalCreators.put(SolidityString.class, this::generateStringValue);
    }

    public String serializeLiteralValue(Variable literal) {
        assert literal != null && literal.getCategory() == VariableCategory.LITERAL;
        assert literal.getValue() != null && literal.getValue().getResult() != null;
        return this.serializeValue(literal.getValue().getResult());
    }

    private String serializeValue(Object value) {
        if (value instanceof String) {
            return String.format("\"%s\"", value.toString());
        }

        if (!List.class.isAssignableFrom(value.getClass())) {
            return value.toString();
        }
        
        @SuppressWarnings("unchecked")
        final String values = ((List<Object>)value).stream()
            .map(obj -> this.serializeValue(obj))
            .collect(Collectors.joining(", "));
        return String.format("{%s}", values);
    }

    public Variable generateLiteral(SolidityType type) {
        assert type != null && this.literalCreators.containsKey(type.getClass());
        final Object value = this.literalCreators.get(type.getClass()).apply(type);
        return new Variable(type, this.createLiteralName(), VariableCategory.LITERAL, value);
    }

    private String createLiteralName() {
        return String.format("literal_%s", this.count++);
    }

    private Object generateAddressValue(SolidityType type) {
        return this.generateBytesString(ADDRESS_LENGTH);
    }

    private Object generateArrayValue(SolidityType type) {
        final SolidityType baseType = ((SolidityArray)type).getBaseType();
        return IntStream.range(0, MIN_LIST_LENGTH + this.random.nextInt(MAX_LIST_LENGTH))
            .mapToObj(i -> this.generateLiteral(baseType))
            .collect(Collectors.toList());
    }

    private Object generateBoolValue(SolidityType type) {
        return Boolean.valueOf(this.random.nextBoolean());
    }
    
    private Object generateBytesValue(SolidityType type) {
        final int length = MIN_BYTES_LENGTH + this.random.nextInt(MAX_BYTES_LENGTH - 1);
        return this.generateBytesString(length);
    }

    private Object generateFixedValue(SolidityType type) {
        return new BigDecimal(this.generateFixedString());
    }

    private Object generateIntegerValue(SolidityType type) {
        return new BigInteger(this.generateIntegerString());
    }

    private Object generateStringValue(SolidityType type) {
        return GeneratorUtils.generateString(STRING_ALPHABET, this.random.nextInt(MAX_STRING_LENGTH));
    }

    private String generateBytesString(int length) {
        return String.format("0x%s", GeneratorUtils.generateString(HEX_DIGITS, 2 * length));
    }

    private String generateFixedString() {
        final String number = this.generateIntegerString();
        
        if (this.random.nextInt(100) < 10) {
            return number;
        }

        return String.format(
            "%s.%s",
            number,
            GeneratorUtils.generateString(DIGITS_WITH_ZERO, this.random.nextInt(MAX_INT_LENGTH))
        );
    }

    private String generateIntegerString() {     
        int length = this.random.nextInt(MAX_INT_LENGTH);
        return length == 0 
            ? GeneratorUtils.generateString(DIGITS_WITH_ZERO, 1)
            : String.format(
                "%s%s", 
                GeneratorUtils.generateString(DIGITS_WITHOUT_ZERO, 1),
                GeneratorUtils.generateString(DIGITS_WITH_ZERO, length)
            )
        ; 
    }
}