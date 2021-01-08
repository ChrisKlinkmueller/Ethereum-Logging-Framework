package blf.library.types;

import java.math.BigInteger;
import java.util.function.BiFunction;

import blf.core.state.ProgramState;

/**
 * IntegerOperations, serves mostly as a wrapper around {@link java.math.BigInteger} operations.
 *
 */
public class IntegerOperations {

    public static Object add(Object[] parameters, ProgramState state) {
        return operate(parameters, BigInteger::add);
    }

    public static Object multiply(Object[] parameters, ProgramState state) {
        return operate(parameters, BigInteger::multiply);
    }

    public static Object subtract(Object[] parameters, ProgramState state) {
        return operate(parameters, BigInteger::subtract);
    }

    public static Object divide(Object[] parameters, ProgramState state) {
        return operate(parameters, BigInteger::divide);
    }

    private static Object operate(Object[] parameters, BiFunction<BigInteger, BigInteger, BigInteger> operation) {
        assert areValidParameters(parameters);
        final BigInteger operand1 = (BigInteger) parameters[0];
        final BigInteger operand2 = (BigInteger) parameters[1];
        return operation.apply(operand1, operand2);
    }

    private static boolean areValidParameters(Object[] parameters) {
        return parameters != null && parameters.length == 2 && parameters[0] instanceof BigInteger && parameters[1] instanceof BigInteger;
    }
}
