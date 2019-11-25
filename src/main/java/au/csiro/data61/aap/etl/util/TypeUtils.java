package au.csiro.data61.aap.etl.util;

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

        if (type.matches(ARRAY_PATTERN) && expectedType.matches(ARRAY_PATTERN)) {
            return areCompatible(type.substring(0, type.length() - 2), expectedType.substring(0, expectedType.length() - 2));
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
}