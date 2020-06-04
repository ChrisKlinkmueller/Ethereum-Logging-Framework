package au.csiro.data61.aap.elf.util;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.web3j.abi.TypeDecoder;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * TypeUtils
 */
public class TypeUtils {
    public static String ARRAY_SUFFIX = "[]";
    public static String ADDRESS_TYPE_KEYWORD = "address";
    public static String BOOL_TYPE_KEYWORD = "bool";
    public static String BYTES_TYPE_KEYWORD = "byte";
    public static String INT_TYPE_KEYWORD = "int";
    public static String STRING_TYPE_KEYWORD = "string";
    private static final String ARRAY_PATTERN = "[a-zA-Z0-9\\[\\]]+\\[\\]";

    public static boolean areCompatible(String type, String expectedType) {
        assert type != null && expectedType != null;

        if (type.equals(expectedType)) {
            return true;
        }

        final boolean typeIsArray = type.matches(ARRAY_PATTERN);
        final boolean expectedTypeIsArray = type.matches(ARRAY_PATTERN);
        if (typeIsArray && expectedTypeIsArray) {
            return areCompatible(type.substring(0, type.length() - 2), expectedType.substring(0, expectedType.length() - 2));
        } else if (typeIsArray || expectedTypeIsArray) {
            return false;
        }

        return (type.contains(INT_TYPE_KEYWORD) && expectedType.contains(INT_TYPE_KEYWORD))
            || (type.contains(BYTES_TYPE_KEYWORD) && expectedType.contains(BYTES_TYPE_KEYWORD))
            || (type.contains(ADDRESS_TYPE_KEYWORD) && expectedType.contains(BYTES_TYPE_KEYWORD))
            || (type.contains(BYTES_TYPE_KEYWORD) && expectedType.contains(ADDRESS_TYPE_KEYWORD))
            || (type.contains(ADDRESS_TYPE_KEYWORD) && expectedType.contains(ADDRESS_TYPE_KEYWORD));
    }

    public static boolean isArrayType(String arrayType, String baseType) {
        assert arrayType != null && baseType != null;
        return isArrayType(arrayType) && arrayType.contains(getRootType(baseType));
    }

    public static String getRootType(String type) {
        assert type != null;
        if (type.equals(ADDRESS_TYPE_KEYWORD)) {
            return ADDRESS_TYPE_KEYWORD;
        } else if (type.equals(BOOL_TYPE_KEYWORD)) {
            return BOOL_TYPE_KEYWORD;
        } else if (type.contains(BYTES_TYPE_KEYWORD)) {
            return BYTES_TYPE_KEYWORD;
        } else if (type.contains(INT_TYPE_KEYWORD)) {
            return INT_TYPE_KEYWORD;
        } else if (type.contains(STRING_TYPE_KEYWORD)) {
            return STRING_TYPE_KEYWORD;
        } else {
            throw new IllegalArgumentException(String.format("''%s' not recognized as a valid type.", type));
        }

    }

    public static String toArrayType(String baseType) {
        assert baseType != null;
        return String.format("%s[]", baseType);
    }

    public static boolean isArrayType(String solType) {
        return solType != null && solType.matches(ARRAY_PATTERN);
    }

    public static boolean isAddressType(String solType) {
        return solType != null && solType.equals(ADDRESS_TYPE_KEYWORD);
    }

    public static boolean isBooleanType(String solType) {
        return solType != null && solType.equals(BOOL_TYPE_KEYWORD);
    }

    public static boolean isBytesType(String solType) {
        return solType != null && solType.equals(BYTES_TYPE_KEYWORD);
    }

    public static boolean isIntegerType(String solType) {
        return solType != null && solType.contains(INT_TYPE_KEYWORD);
    }

    public static boolean isStringType(String solType) {
        return solType != null && solType.contains(STRING_TYPE_KEYWORD);
    }

    public static Object convertValueTo(String solidityType, Object value) throws ProgramException {
        assert solidityType != null && value != null;
        try {
            return TypeDecoder.instantiateType(solidityType, value).getValue();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException
            | ClassNotFoundException e) {
            throw new ProgramException(String.format("Error when decoding value '%s' as '%s'", value, solidityType), e);
        }
    }

    private static final String INTEGER_PATTERN = "^-?\\d+";

    public static BigInteger integerFromLiteral(String literal) {
        if (!literal.matches(INTEGER_PATTERN)) {
            return null;
        }

        return new BigInteger(literal);
    }

    public static boolean isArrayLiteral(String literal) {
        return literal != null && 2 <= literal.length() && literal.charAt(0) == '[' && literal.charAt(literal.length() - 1) == ']';
    }

    private static final int ADDRESS_BYTES_LENGTH = 20;

    public static boolean isAddressLiteral(String literal) {
        return isBytesLiteral(literal, ADDRESS_BYTES_LENGTH);
    }

    private static final String BYTES_PATTERN = "0[xX][0-9a-fA-F]+";

    public static boolean isBytesLiteral(String literal, int bytesLength) {
        assert 0 < bytesLength && bytesLength <= 32;
        if (literal == null) {
            return false;
        }
        final boolean matchesBytesPattern = literal.matches(BYTES_PATTERN);
        final boolean hasLength = literal.length() == 2 * (bytesLength + 1);
        return matchesBytesPattern && hasLength;
    }

    public static boolean isBytesLiteral(String literal) {
        return literal != null && literal.matches(BYTES_PATTERN);
    }

    public static boolean isBooleanLiteral(String literal) {
        return literal != null && (literal.toLowerCase().equals("true") || literal.toLowerCase().equals("false"));
    }

    private static final String INT_PATTERN = "[0]|[1-9][0-9]*";

    public static boolean isIntLiteral(String literal) {
        return literal != null && literal.matches(INT_PATTERN);
    }

    public static boolean isStringLiteral(String literal) {
        return literal != null && 2 <= literal.length() && literal.charAt(0) == '\"' && literal.charAt(literal.length() - 1) == '\"';
    }

    public static Boolean parseBoolLiteral(String literal) {
        if (!TypeUtils.isBooleanLiteral(literal)) {
            throw new IllegalArgumentException(String.format("Value '%s' is not a string literal.", literal));
        }
        return Boolean.parseBoolean(literal);
    }

    public static List<Boolean> parseBoolArrayLiteral(String literal) {
        return parseArrayLiteral(literal, TypeUtils::parseBoolLiteral);
    }

    public static String parseBytesLiteral(String literal) {
        if (!TypeUtils.isBytesLiteral(literal)) {
            throw new IllegalArgumentException(String.format("Value '%s' is not a bytes or address literal.", literal));
        }
        return literal;
    }

    public static List<String> parseBytesArrayLiteral(String literal) {
        return parseArrayLiteral(literal, TypeUtils::parseBytesLiteral);
    }

    public static BigInteger parseIntLiteral(String literal) {
        if (!TypeUtils.isIntLiteral(literal)) {
            throw new IllegalArgumentException(String.format("Value '%s' is not an int literal.", literal));
        }
        return new BigInteger(literal);
    }

    public static List<BigInteger> parseIntArrayLiteral(String literal) {
        return parseArrayLiteral(literal, TypeUtils::parseIntLiteral);
    }

    public static String parseStringLiteral(String literal) {
        if (!TypeUtils.isStringLiteral(literal)) {
            throw new IllegalArgumentException(String.format("Value '%s' is not a string literal.", literal));
        }
        return literal.substring(1, literal.length() - 1);
    }

    public static List<String> parseStringArrayLiteral(String literal) {
        return parseArrayLiteral(literal, TypeUtils::parseStringLiteral);
    }

    private static <T> List<T> parseArrayLiteral(String literal, Function<String, T> converter) {
        if (!TypeUtils.isArrayLiteral(literal)) {
            throw new IllegalArgumentException(String.format("Value '%s' is not an array literal.", literal));
        }
        List<T> list = new ArrayList<>();
        final String[] elements = literal.substring(1, literal.length() - 1).split(",");
        for (String element : elements) {
            list.add(converter.apply(element));
        }
        return list;
    }

    @FunctionalInterface
    private static interface Converter<T> {
        public T convert(String literal);
    }
}
