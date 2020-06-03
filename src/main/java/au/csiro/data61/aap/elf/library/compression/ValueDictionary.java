package au.csiro.data61.aap.elf.library.compression;

import java.math.BigInteger;
import java.util.List;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * ValueDictionary
 */
public class ValueDictionary {
    public static final String METHOD_NAME = "mapValue";

    public static Object boolToBool(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, Boolean.class, Boolean.class);
    }

    public static Object boolToInt(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, Boolean.class, BigInteger.class);
    }

    public static Object boolToString(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, Boolean.class, String.class);
    }

    public static Object intToBool(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, BigInteger.class, Boolean.class);
    }

    public static Object intToInt(Object[] parameters, ProgramState state) throws ProgramException {
        return mapValue(parameters, BigInteger.class, BigInteger.class);
    }

    public static Object intToString(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, BigInteger.class, String.class);
    }

    public static Object stringToBool(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, String.class, Boolean.class);
    }

    public static Object stringToInt(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, String.class, BigInteger.class);
    }

    public static Object stringToString(Object[] parameters, ProgramState state)
            throws ProgramException {
        return mapValue(parameters, String.class, String.class);
    }

    @SuppressWarnings("all")
    private static <S, T> Object mapValue(Object[] parameters, Class<S> sourceClass,
            Class<T> targetClass) throws ProgramException {
        if (!areValidParameters(parameters, sourceClass, targetClass)) {
            throw new ProgramException(String.format(
                    "Invalid parameters for mapping from %s to %s.", sourceClass, targetClass));
        }

        S value = (S) parameters[0];
        T defaultTarget = (T) parameters[1];
        List<S> sourceValues = (List<S>) parameters[2];
        List<T> targetValues = (List<T>) parameters[3];
        return mapValue(value, defaultTarget, sourceValues, targetValues);
    }

    @SuppressWarnings("all")
    private static <S, T> boolean areValidParameters(Object[] parameters, Class<S> sourceClass,
            Class<T> targetClass) {
        if (parameters.length != 4
                || (parameters[0] != null && !parameters[0].getClass().equals(sourceClass))
                || (parameters[1] != null && !parameters.getClass().equals(targetClass))) {
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

    private static <S, T> T mapValue(S value, T defaultTarget, List<S> sourceValues,
            List<T> targetValues) {
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
        return value1 == null ? value2 == null : value1.equals(value2);
    }
}
