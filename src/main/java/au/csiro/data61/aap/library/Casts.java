package au.csiro.data61.aap.library;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.ProgramState;
import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityBool;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityFixed;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.ValueCasts;

/**
 * Casts
 */
class Casts {
    private static final List<Method> CASTS;
    
    static Stream<Method> castStream() {
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

    private static Object addressToBytes(ProgramState state, Object[] parameters) throws Throwable {
        assert Library.isValidParameterList(parameters, String.class);
        return ValueCasts.addressToBytes(parameters[0]);
    }    

    private static Object addressToString(ProgramState state, Object[] parameters) throws Throwable {
        assert Library.isValidParameterList(parameters, String.class);
        return ValueCasts.addressToString(parameters[0]);
    }

    private static Object boolToString(ProgramState state, Object[] parameters) {
        assert Library.isValidParameterList(parameters, Boolean.class);
        return ValueCasts.boolToString(parameters[0]);
    }

    private static Object bytesToAddress(ProgramState state, Object[] parameters) throws Throwable {
        assert Library.isValidParameterList(parameters, String.class);
        return ValueCasts.bytesToAddress(parameters[0]);
    }

    private static Object bytesToString(ProgramState state, Object[] parameters) throws Throwable {
        assert Library.isValidParameterList(parameters, String.class);
        return ValueCasts.bytesToString(parameters[0]);
    }

    private static Object fixedToInteger(ProgramState state, Object[] parameters) {
        assert Library.isValidParameterList(parameters, BigDecimal.class);
        return ValueCasts.fixedToInteger(parameters[0]);
    }

    private static Object fixedToString(ProgramState state, Object[] parameters) {
        assert Library.isValidParameterList(parameters, BigDecimal.class);
        return ValueCasts.fixedToString(parameters[0]);
    }

    private static Object integerToFixed(ProgramState state, Object[] parameters) {
        assert Library.isValidParameterList(parameters, BigInteger.class);
        return ValueCasts.integerToFixed(parameters[0]);
    }

    private static Object integerToString(ProgramState state, Object[] parameters) {
        assert Library.isValidParameterList(parameters, BigInteger.class);
        return ValueCasts.integerToString(parameters[0]);
    }
}