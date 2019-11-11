package au.csiro.data61.aap.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityString;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * ScriptGeneratorUtils
 */
public class ScriptGeneratorUtils {
    private static final Random RANDOM = new Random();
    private static final String STRING_LITERAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz .:0123456789?*!@#$%^&()";
    private static final String BYTES_LITERAL_ALPHABET = "ABCDEFabcdef0123456789";
    private static final String NUMBER_LITERAL_ALPHABET_FULL = "0123456789";
    private static final String NUMBER_LITERAL_ALPHABET_SHORT = "123456789";
    private static final String VARIABLE_LITERAL_FIRST = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String VARIABLE_LITERAL_FULL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final Map<SolidityType, Supplier<String>> LITERAL_BUILDERS;
    private static final Map<SolidityType, Supplier<String>> TYPE_BUILDERS;

    static {
        LITERAL_BUILDERS = new HashMap<>();
        LITERAL_BUILDERS.put(SolidityAddress.DEFAULT_INSTANCE, ScriptGeneratorUtils::getAddressLiteral);
        LITERAL_BUILDERS.put(new SolidityArray(SolidityAddress.DEFAULT_INSTANCE), () -> getArrayLiteral(ScriptGeneratorUtils::getAddressLiteral));
        LITERAL_BUILDERS.put(SolidityBool.DEFAULT_INSTANCE, ScriptGeneratorUtils::getBoolLiteral);
        LITERAL_BUILDERS.put(new SolidityArray(SolidityBool.DEFAULT_INSTANCE), () -> getArrayLiteral(ScriptGeneratorUtils::getBoolLiteral));
        LITERAL_BUILDERS.put(SolidityBytes.DEFAULT_INSTANCE, ScriptGeneratorUtils::getBytesLiteral);
        LITERAL_BUILDERS.put(new SolidityArray(SolidityBytes.DEFAULT_INSTANCE), () -> getArrayLiteral(ScriptGeneratorUtils::getBytesLiteral));
        LITERAL_BUILDERS.put(SolidityFixed.DEFAULT_INSTANCE, ScriptGeneratorUtils::getFixedLiteral);
        LITERAL_BUILDERS.put(new SolidityArray(SolidityFixed.DEFAULT_INSTANCE), () -> getArrayLiteral(ScriptGeneratorUtils::getFixedLiteral));
        LITERAL_BUILDERS.put(SolidityInteger.DEFAULT_INSTANCE, ScriptGeneratorUtils::getIntegerLiteral);
        LITERAL_BUILDERS.put(new SolidityArray(SolidityInteger.DEFAULT_INSTANCE), () -> getArrayLiteral(ScriptGeneratorUtils::getIntegerLiteral));
        LITERAL_BUILDERS.put(SolidityString.DEFAULT_INSTANCE, ScriptGeneratorUtils::getStringLiteral);
        LITERAL_BUILDERS.put(new SolidityArray(SolidityString.DEFAULT_INSTANCE), () -> getArrayLiteral(ScriptGeneratorUtils::getStringLiteral));
        
        TYPE_BUILDERS = new HashMap<>();
        TYPE_BUILDERS.put(SolidityAddress.DEFAULT_INSTANCE, ScriptGeneratorUtils::getAddressType);
        TYPE_BUILDERS.put(new SolidityArray(SolidityAddress.DEFAULT_INSTANCE), () -> getArrayType(ScriptGeneratorUtils::getAddressType));
        TYPE_BUILDERS.put(SolidityBool.DEFAULT_INSTANCE, ScriptGeneratorUtils::getBoolType);
        TYPE_BUILDERS.put(new SolidityArray(SolidityBool.DEFAULT_INSTANCE), () -> getArrayType(ScriptGeneratorUtils::getBoolType));
        TYPE_BUILDERS.put(SolidityBytes.DEFAULT_INSTANCE, ScriptGeneratorUtils::getBytesType);
        TYPE_BUILDERS.put(new SolidityArray(SolidityBytes.DEFAULT_INSTANCE), () -> getArrayType(ScriptGeneratorUtils::getBytesType));
        TYPE_BUILDERS.put(SolidityFixed.DEFAULT_INSTANCE, ScriptGeneratorUtils::getFixedType);
        TYPE_BUILDERS.put(new SolidityArray(SolidityFixed.DEFAULT_INSTANCE), () -> getArrayType(ScriptGeneratorUtils::getFixedType));
        TYPE_BUILDERS.put(SolidityInteger.DEFAULT_INSTANCE, ScriptGeneratorUtils::getIntegerType);
        TYPE_BUILDERS.put(new SolidityArray(SolidityInteger.DEFAULT_INSTANCE), () -> getArrayType(ScriptGeneratorUtils::getIntegerType));
        TYPE_BUILDERS.put(SolidityString.DEFAULT_INSTANCE, ScriptGeneratorUtils::getStringType);
        TYPE_BUILDERS.put(new SolidityArray(SolidityString.DEFAULT_INSTANCE), () -> getArrayType(ScriptGeneratorUtils::getStringType));
    }

    public static String createStatement() {
        final int index = RANDOM.nextInt(TYPE_BUILDERS.size());
        final SolidityType type = TYPE_BUILDERS.keySet().stream().collect(Collectors.toList()).get(index);
        return createStatement(type);
    }

    public static String createStatement(SolidityType type) {
        return String.format(
            "%s %s = %s;",
            createType(type),
            createVariableName(),
            createLiteral(type)
        );
    }

    public static String createVariableName() {
        final String firstPart = createVariableNamePart();

        if (RANDOM.nextBoolean()) {
            return firstPart;
        }
        
        return String.format(
            "%s:%s", 
            firstPart,
            createVariableNamePart()
        );
    }

    private static String createVariableNamePart() {
        return String.format(
            "%s%s", 
            createLiteral(VARIABLE_LITERAL_FIRST, 1),
            createLiteral(VARIABLE_LITERAL_FULL, 10)
        );
    }

    public static String createLiteral(SolidityType type) {
        assert type != null;
        return LITERAL_BUILDERS.get(type).get();
    }

    public static String createType(SolidityType type) {
        assert type != null;
        return TYPE_BUILDERS.get(type).get();
    }

    private static String getArrayLiteral(Supplier<String> literalBuilder) {
        final String values = IntStream.range(0, 1 + RANDOM.nextInt(10))
            .mapToObj(i -> literalBuilder.get())
            .collect(Collectors.joining(", "));
        return String.format("{%s}", values);
    }

    private static String getAddressLiteral() {
        return String.format("0x%s", createLiteral(BYTES_LITERAL_ALPHABET, 40));
    }

    private static String getBoolLiteral() {
        return RANDOM.nextBoolean() ? Boolean.toString(Boolean.TRUE) : Boolean.toString(Boolean.FALSE) ;
    }

    private static String getBytesLiteral() {
        final int length = 2 * (1 + RANDOM.nextInt(32));
        return String.format("0x%s", createLiteral(BYTES_LITERAL_ALPHABET, length));
    }

    private static String getFixedLiteral() {
        final int digitLength = RANDOM.nextInt(11);
        if (digitLength == 0) {
            return getIntegerLiteral();
        }
        return String.format(
            "%s.%s", 
            getIntegerLiteral(),
            createLiteral(NUMBER_LITERAL_ALPHABET_FULL, digitLength)
        );
    }

    private static String getIntegerLiteral() {
        final int length = 1 + RANDOM.nextInt(18);
        return length == 1 
            ? createLiteral(NUMBER_LITERAL_ALPHABET_FULL, length)
            : String.format(
                "%s%s",
                createLiteral(NUMBER_LITERAL_ALPHABET_SHORT, 1),
                createLiteral(NUMBER_LITERAL_ALPHABET_FULL, length - 1)
            );
    }

    private static String getStringLiteral() {
        final int length = 4 + RANDOM.nextInt(30);
        return String.format("\\\"%s\\\"", createLiteral(STRING_LITERAL_ALPHABET, length));
    }

    private static String createLiteral(String alphabet, int length) {
        return IntStream.range(0, length)
            .mapToObj(i -> Character.toString(alphabet.charAt(RANDOM.nextInt(alphabet.length()))))
            .collect(Collectors.joining());
    }

    private static String getArrayType(Supplier<String> typeBuilder) {
        return String.format("%s[]", typeBuilder.get());
    }


    private static String getAddressType() {
        return "address";
    }

    private static String getBoolType() {
        return "bool";
    }

    private static String getBytesType() {
        final int length = RANDOM.nextInt(34);
        if (length == 0) {
            return "byte";
        }
        else if (length == 33) {
            return "bytes";
        }
        else {
            return String.format("bytes%s", length);
        }
    }

    private static String getFixedType() {
        final String signed = nextSigned();
        final int bitLength = nextBitLength();

        if (bitLength == 0) {
            return String.format("%sfixed", signed);
        }

        return String.format("%sfixed%sx%s", signed, bitLength, RANDOM.nextInt(81));
    }

    private static String getIntegerType() {
        final String signed = nextSigned();
        final int bitLength = nextBitLength();

        if (bitLength == 0) {
            return String.format("%sint", signed);
        }
        return String.format("%sint%s", signed, bitLength);
    }

    private static String getStringType() {
        return "string";
    }

    private static int nextBitLength() {
        return RANDOM.nextInt(33) * 8;
    }

    private static String nextSigned() {
        return RANDOM.nextBoolean() ? "" : "u";
    }

    public static void main(String[] args) {
        TYPE_BUILDERS.keySet().forEach(
            t -> System.out.println(createStatement(t))
        );
    }
}