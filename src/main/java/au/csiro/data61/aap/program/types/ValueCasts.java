package au.csiro.data61.aap.program.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * SolidityTypeCasts
 */
public class ValueCasts {
    private static final String BYTES_PATTERN = "0x[0-9a-fA-F]*";
    private static final int ADDRESS_LENGTH = 42;
    private static final int MINIMUM_BYTES_LENGTH = 4;
    private static final int MAXIMUM_BYTES_LENGTH = 66;
    private static final Map<Class<? extends SolidityType>, Map<Class<? extends SolidityType>, ValueCast>> CASTS;

    static {
        CASTS = new HashMap<>();
        addCast(SolidityAddress.class, SolidityBytes.class, ValueCasts::addressToBytes);
        addCast(SolidityAddress.class, SolidityString.class, ValueCasts::addressToString);
        addCast(SolidityBool.class, SolidityString.class, ValueCasts::boolToString);
        addCast(SolidityBytes.class, SolidityAddress.class, ValueCasts::bytesToAddress);
        addCast(SolidityBytes.class, SolidityString.class, ValueCasts::bytesToString);
        addCast(SolidityFixed.class, SolidityInteger.class, ValueCasts::fixedToInteger);
        addCast(SolidityFixed.class, SolidityString.class, ValueCasts::fixedToString);
        addCast(SolidityInteger.class, SolidityFixed.class, ValueCasts::integerToFixed);
        addCast(SolidityInteger.class, SolidityString.class, ValueCasts::integerToString);
    }

    private static void addCast(Class<? extends SolidityType> from, Class<? extends SolidityType> to, ValueCast cast) {
        CASTS.putIfAbsent(from, new HashMap<>()).put(to, cast);
    }

    public static boolean isCastSupported(SolidityType from,SolidityType to) {
        if (from.conceptuallyEquals(to)) {
            return true;
        }

        return isCastSupported(from.getClass(), to.getClass());
    }

    public static boolean isCastSupported(Class<? extends SolidityType> from, Class<? extends SolidityType> to) {
        return CASTS.containsKey(from) && CASTS.get(from).containsKey(to);
    }

    public static Object addressToBytes(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        if (isAddressLiteral((String)value)) {
            return value;
        }
        throw new ValueCastException(String.format("'%s' is not a valid address value.", value));
    }

    public static Object addressToString(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        if (isAddressLiteral((String)value)) {
            return value;
        }
        throw new ValueCastException(String.format("'%s' is not a valid address value.", value));
    }    

	public static Object boolToString(Object value) {
		assert value != null && value instanceof Boolean;
        return Boolean.toString((Boolean)value);
    }
    
    public static Object bytesToAddress(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        if (isAddressLiteral((String)value)) {
            return value;            
        }
        throw new ValueCastException(String.format("'%s' is not a valid address value.", value));
    }

    public static Object bytesToString(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        if (isBytesLiteral((String)value)) {
            return value;
        }
        throw new ValueCastException(String.format("'%s' is not a valid bytes value.", value));
    }

    public static Object fixedToInteger(Object value) {
        assert value != null && value instanceof BigDecimal;
        return ((BigDecimal)value).toBigInteger();
    }

    public static Object fixedToString(Object value) {
        assert value != null && value instanceof BigDecimal;
        return ((BigDecimal)value).toString();
    }

    public static Object integerToFixed(Object value) {
        assert value != null && value instanceof BigInteger;
        return new BigDecimal((BigInteger)value);
    }

    public static Object integerToString(Object value) {
        assert value != null && value instanceof BigInteger;
        return ((BigInteger)value).toString();
    }

    public static Object stringToAddress(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        if (isAddressLiteral((String)value)) {
            return value;
        }
        throw new ValueCastException(String.format("'%s' is not a valid address value.", value));
    }

    public static Object stringToBytes(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        if (isBytesLiteral((String)value)) {
            return value;
        }
        throw new ValueCastException(String.format("'%s' is not a valid bytes value.", value));
    }

    public static Object stringToInteger(Object value) throws ValueCastException {
        assert value != null && value instanceof String;
        try {
            return new BigInteger((String)value);
        } catch (Throwable ex) {
            throw new ValueCastException(String.format("Error casting string '%s' to integer.", value), ex);
        }
    }

    public static boolean isAddressLiteral(String literal) {
        assert literal != null;
        return literal.matches(BYTES_PATTERN) && literal.length() != ADDRESS_LENGTH;
    }

    public static boolean isBytesLiteral(String literal) {
        return     literal.matches(BYTES_PATTERN) 
                && literal.length() % 2 == 0
                && MINIMUM_BYTES_LENGTH <= literal.length()
                && literal.length() <= MAXIMUM_BYTES_LENGTH;
    }
    
    @FunctionalInterface
    public static interface ValueCast {
        public Object cast(Object value) throws ValueCastException;
    }

    public static class ValueCastException extends Exception {
        private static final long serialVersionUID = 1L;

        public ValueCastException(String message) {
            super(message);
        }

        public ValueCastException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
}