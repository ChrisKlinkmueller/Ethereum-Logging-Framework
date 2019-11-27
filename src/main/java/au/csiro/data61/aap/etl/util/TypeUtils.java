package au.csiro.data61.aap.etl.util;

import java.lang.reflect.InvocationTargetException;

import org.web3j.abi.TypeDecoder;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * TypeUtils
 */
public class TypeUtils {
    public static String ADDRESS_TYPE_KEYWORD = "address";
    public static String BOOL_TYPE_KEYWORD = "bool";
    public static String BYTES_TYPE_KEYWORD = "byte";
    public static String FIXED_TYPE_KEYWORD = "fixed";
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
            return areCompatible(type.substring(0, type.length() - 2),
                    expectedType.substring(0, expectedType.length() - 2));
        } else if (typeIsArray || expectedTypeIsArray) {
            return false;
        }

        return (type.contains(INT_TYPE_KEYWORD) && expectedType.contains(INT_TYPE_KEYWORD))
                || (type.contains(FIXED_TYPE_KEYWORD) && expectedType.contains(FIXED_TYPE_KEYWORD))
                || (type.contains(BYTES_TYPE_KEYWORD) && expectedType.contains(BYTES_TYPE_KEYWORD))
                || (type.contains(ADDRESS_TYPE_KEYWORD) && expectedType.contains(BYTES_TYPE_KEYWORD))
                || (type.contains(BYTES_TYPE_KEYWORD) && expectedType.contains(ADDRESS_TYPE_KEYWORD));
    }

    public static boolean hasBaseType(String testType, String expectedType) {
        assert testType != null && expectedType != null;
        return testType.contains(expectedType);
    }

    public static String getArrayType(String baseType) {
        assert baseType != null;
        return String.format("%s[]", baseType);
    }

    public static boolean isArrayType(String type) {
        return type != null && type.matches(ARRAY_PATTERN);
    }

    public static Object convertValueTo(String solidityType, Object value) throws ProgramException {
        assert solidityType != null && value != null;
        try {
            return TypeDecoder.instantiateType(solidityType, value).getValue();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ProgramException(String.format("Error when decoding value '%s' as '%s'", value, solidityType), e);
        }
    }
}