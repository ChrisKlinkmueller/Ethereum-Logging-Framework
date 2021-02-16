package blf.library.types;

import java.math.BigInteger;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;
import blf.library.util.TriFunction;

/**
 * IntegerOperations, serves mostly as a wrapper around {@link java.math.BigInteger} operations.
 *
 */
public class StringOperations {

    private StringOperations() {}

    public static List<String> split(Object[] parameters, ProgramState state) {
        return operate(
            state,
            parameters,
            (string, value) -> Stream.of((string).split(value)).map(String::new).collect(Collectors.toList())
        );
    }

    public static Boolean matches(Object[] parameters, ProgramState state) {
        return operate(state, parameters, String::matches);
    }

    public static BigInteger length(Object[] parameters, ProgramState state) {
        return operate(state, parameters, string -> BigInteger.valueOf(string.length()));
    }

    public static String replaceFirst(Object[] parameters, ProgramState state) {
        return operate(state, parameters, String::replaceFirst);
    }

    public static String replaceAll(Object[] parameters, ProgramState state) {
        return operate(state, parameters, String::replaceAll);
    }

    private static <T> T operate(ProgramState state, Object[] parameters, Function<String, T> operation) {
        if (!areValidParametersFunction(parameters)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return null;
        }

        final String operand = (String) parameters[0];

        try {
            return operation.apply(operand);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return null;
    }

    private static <T> T operate(ProgramState state, Object[] parameters, BiFunction<String, String, T> operation) {
        if (!areValidParametersBiFunction(parameters)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return null;
        }

        final String operand1 = (String) parameters[0];
        final String operand2 = (String) parameters[1];
        try {
            return operation.apply(operand1, operand2);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return null;
    }

    private static <T> T operate(ProgramState state, Object[] parameters, TriFunction<String, String, String, T> operation) {
        if (!areValidParametersTriFunction(parameters)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return null;
        }

        final String operand1 = (String) parameters[0];
        final String operand2 = (String) parameters[1];
        final String operand3 = (String) parameters[2];

        try {
            return operation.apply(operand1, operand2, operand3);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return null;
    }

    private static boolean areValidParametersFunction(Object[] parameters) {
        return parameters != null && parameters.length == 1 && parameters[0] instanceof String;
    }

    private static boolean areValidParametersBiFunction(Object[] parameters) {
        return parameters != null && parameters.length == 2 && parameters[0] instanceof String && parameters[1] instanceof String;
    }

    private static boolean areValidParametersTriFunction(Object[] parameters) {
        return parameters != null
            && parameters.length == 3
            && parameters[0] instanceof String
            && parameters[1] instanceof String
            && parameters[2] instanceof String;
    }

}
