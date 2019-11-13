package au.csiro.data61.aap.library;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import au.csiro.data61.aap.parser.AnalyzerUtils;
import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityBool;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityFixed;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;

/**
 * Casts
 */
public class Casts {
    private static final List<Method> CASTS;
    
    public static Stream<Method> castStream() {
        return CASTS.stream();
    }

    static {
        CASTS = new ArrayList<>();
        CASTS.add(new Method(Casts::addressToBytes, SolidityBytes.DEFAULT_INSTANCE, "toBytes", SolidityAddress.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::addressToString, SolidityString.DEFAULT_INSTANCE, "toString", SolidityAddress.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::boolToString, SolidityString.DEFAULT_INSTANCE, "toString", SolidityBool.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::bytesToAddress, SolidityAddress.DEFAULT_INSTANCE, "toBytes", SolidityBytes.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::bytesToString, SolidityString.DEFAULT_INSTANCE, "toString", SolidityBytes.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::integerToString, SolidityString.DEFAULT_INSTANCE, "toString", SolidityInteger.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::integerToFixed, SolidityFixed.DEFAULT_INSTANCE, "toFixed", SolidityInteger.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::fixedToInteger, SolidityInteger.DEFAULT_INSTANCE, "toInteger", SolidityFixed.DEFAULT_INSTANCE));
        CASTS.add(new Method(Casts::fixedToString, SolidityString.DEFAULT_INSTANCE, "toString", SolidityFixed.DEFAULT_INSTANCE));
    }

    private static Object addressToBytes(Object[] parameters) {
        assert isValidParameterList(parameters, String.class);
        assert AnalyzerUtils.isAddressLiteral((String)parameters[0]);
        return parameters[0];
    }    

    private static Object addressToString(Object[] parameters) {
        assert isValidParameterList(parameters, String.class);
        assert AnalyzerUtils.isAddressLiteral((String)parameters[0]);
        return parameters[0];
    }

    private static Object boolToString(Object[] parameters) {
        assert isValidParameterList(parameters, Boolean.class);
        return Boolean.toString((Boolean)parameters[0]);
    }

    private static Object bytesToAddress(Object[] parameters) {
        assert isValidParameterList(parameters, String.class);
        if (AnalyzerUtils.isAddressLiteral((String)parameters[0])) {
            return parameters[0];
        }
        throw new IllegalArgumentException(String.format("The bytes value '%s' is not a valid address value.", parameters[0]));
    }

    private static Object bytesToString(Object[] parameters) {
        assert isValidParameterList(parameters, String.class);
        return parameters[0];
    }

    private static Object integerToString(Object[] parameters) {
        assert isValidParameterList(parameters, BigInteger.class);
        return ((BigInteger)parameters[0]).toString();
    }

    private static Object integerToFixed(Object[] parameters) {
        assert isValidParameterList(parameters, BigInteger.class);
        return new BigDecimal((BigInteger)parameters[0]);
    }

    private static Object fixedToInteger(Object[] parameters) {
        assert isValidParameterList(parameters, BigDecimal.class);
        return ((BigDecimal)parameters[0]).toBigInteger();
    }

    private static Object fixedToString(Object[] parameters) {
        assert isValidParameterList(parameters, BigDecimal.class);
        return ((BigDecimal)parameters[0]).toString();
    }

    private static boolean isValidParameterList(Object[] parameters, Class<?> cl) {
        return parameters != null && parameters.length == 1 && parameters[0].getClass().equals(cl);
    }

    public static void main(String[] args) {
        final BigInteger integer = new BigInteger("10000");
        for (Byte b1 : integer.toByteArray()) {
            System.out.println(b1);
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            System.out.println(s1);
            System.out.println(Byte.parseByte(s1, 2));
            System.out.println();
        }

        BigInteger newInt = new BigInteger("FF", 16);
        System.out.println(newInt);
        System.out.println(newInt.toString(16));

        BigDecimal dec = new BigDecimal("100102.06");
        System.out.println(dec.toBigInteger());
        
    }
}