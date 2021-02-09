package blf.library.compression;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * ValueDictionary
 */
public class ValueDictionary {
    public static final String METHOD_NAME = "mapValue";

    private ValueDictionary() {}

    public static Object boolToBool(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, Boolean.class, Boolean.class);
    }

    public static Object boolToInt(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, Boolean.class, BigInteger.class);
    }

    public static Object boolToString(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, Boolean.class, String.class);
    }

    public static Object intToBool(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, BigInteger.class, Boolean.class);
    }

    public static Object intToInt(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, BigInteger.class, BigInteger.class);
    }

    public static Object intToString(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, BigInteger.class, String.class);
    }

    public static Object stringToBool(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, String.class, Boolean.class);
    }

    public static Object stringToInt(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, String.class, BigInteger.class);
    }

    public static Object stringToString(Object[] parameters, ProgramState state) {
        return mapValue(state, parameters, String.class, String.class);
    }

    @SuppressWarnings("all")
    private static <S, T> Object mapValue(ProgramState state, Object[] parameters, Class<S> sourceClass, Class<T> targetClass) {
        if (!areValidParameters(parameters, sourceClass, targetClass)) {
            final String errorMsg = String.format(
                "Invalid parameters for mapping from %s to %s, expected '%s' but got '%s'.",
                sourceClass,
                targetClass
            );

            ExceptionHandler.getInstance().handleException(errorMsg, new Exception());

            return null;
        }

        S value = (S) parameters[0];
        T defaultTarget = (T) parameters[1];
        List<S> sourceValues = (List<S>) parameters[2];
        List<T> targetValues = (List<T>) parameters[3];

        return mapValue(value, defaultTarget, sourceValues, targetValues);
    }

    @SuppressWarnings("all")
    private static <S, T> boolean areValidParameters(Object[] parameters, Class<S> sourceClass, Class<T> targetClass) {
        if (parameters.length != 4
            || (parameters[0] != null && !parameters[0].getClass().equals(sourceClass))
            || (parameters[1] != null && !parameters[1].getClass().equals(targetClass))) {
            return false;
        }

        try {
            List<S> value1 = (List<S>) parameters[2];
            List<T> value2 = (List<T>) parameters[3];
        } catch (ClassCastException ex) {
            return false;
        }

        return true;
    }

    private static <S, T> T mapValue(S value, T defaultTarget, List<S> sourceValues, List<T> targetValues) {
        assert sourceValues != null;
        assert targetValues != null;

        final int length = Math.min(sourceValues.size(), targetValues.size());
        for (int i = 0; i < length; i++) {
            if (valuesMatch(value, sourceValues.get(i))) {
                return targetValues.get(i);
            }
        }

        return defaultTarget;
    }

    private static <S> boolean valuesMatch(S value1, S value2) {
        return Objects.equals(value1, value2);
    }
}
