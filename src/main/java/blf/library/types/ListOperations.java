package blf.library.types;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;
import io.reactivex.functions.BiFunction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * ListOperations
 */
public class ListOperations {

    private ListOperations() {}

    public static Object newAddressArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newBoolArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newByteArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newIntArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object newStringArray(Object[] parameters, ProgramState state) {
        return new ArrayList<>();
    }

    public static Object addElement(Object[] parameters, ProgramState state) {
        return operate(state, parameters, (list, value) -> {
            list.add(value);
            return list;
        });
    }

    public static Object removeElement(Object[] parameters, ProgramState state) {
        return operate(state, parameters, (list, value) -> {
            list.remove(value);
            return list;
        });
    }

    public static Object clear(Object[] parameters, ProgramState state) {
        return operate(state, parameters, list -> {
            list.clear();
            return list;
        });
    }

    public static Boolean contains(Object[] parameters, ProgramState state) {
        return operate(state, parameters, List::contains);
    }

    public static Object get(Object[] parameters, ProgramState state) {
        return operate(state, parameters, (list, value) -> list.get(((BigInteger) value).intValue()));
    }

    public static BigInteger reduceToSum(Object[] parameters, ProgramState state) {
        return operate(state, parameters, list -> list.stream().map(num -> (BigInteger) num).reduce(BigInteger::add).get());
    }

    public static BigInteger reduceToProduct(Object[] parameters, ProgramState state) {
        return operate(state, parameters, list -> list.stream().map(num -> (BigInteger) num).reduce(BigInteger::multiply).get());
    }

    public static String reduceToString(Object[] parameters, ProgramState state) {
        return operate(state, parameters, list -> list.stream().map(Object::toString).reduce("", String::concat));
    }

    @SuppressWarnings("unchecked")
    private static <T> T operate(ProgramState state, Object[] parameters, Function<List<Object>, T> operation) {
        if (!areValidParametersFunction(parameters)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return null;
        }

        final List<Object> operand1 = (List<Object>) parameters[0];

        try {
            return operation.apply(operand1);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T operate(ProgramState state, Object[] parameters, BiFunction<List<Object>, Object, T> operation) {
        if (!areValidParametersBiFunction(parameters)) {
            ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

            return null;
        }

        final List<Object> operand1 = (List<Object>) parameters[0];
        final Object operand2 = parameters[1];

        try {
            return operation.apply(operand1, operand2);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Error executing method call.", e);
        }

        return null;
    }

    private static boolean areValidParametersFunction(Object[] parameters) {
        return parameters != null
            && parameters.length == 1
            && parameters[0] != null
            && List.class.isAssignableFrom(parameters[0].getClass());

    }

    private static boolean areValidParametersBiFunction(Object[] parameters) {
        return parameters != null
            && parameters.length == 2
            && parameters[0] != null
            && parameters[1] != null
            && List.class.isAssignableFrom(parameters[0].getClass());

    }

}
