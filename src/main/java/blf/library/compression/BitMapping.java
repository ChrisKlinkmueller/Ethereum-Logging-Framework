package blf.library.compression;

import blf.core.state.ProgramState;

import java.math.BigInteger;
import java.util.List;

/**
 * BitMapping
 */
public class BitMapping {
    public static final String METHOD_NAME = "mapBits";

    private BitMapping() {}

    public static Object mapBitsToBool(Object[] parameters, ProgramState state) {
        return mapBits(state, parameters, Boolean.class);
    }

    public static Object mapBitsToInt(Object[] parameters, ProgramState state) {
        return mapBits(state, parameters, BigInteger.class);
    }

    public static Object mapBitsToString(Object[] parameters, ProgramState state) {
        return mapBits(state, parameters, String.class);
    }

    @SuppressWarnings("all")
    private static <T> T mapBits(ProgramState state, Object[] parameters, Class<T> cl) {
        try {
            final BigInteger value = (BigInteger) parameters[0];
            final int from = ((BigInteger) parameters[1]).intValue();
            final int to = ((BigInteger) parameters[2]).intValue();
            final List<T> values = (List<T>) parameters[3];

            return mapBits(value, from, to, values);
        } catch (Exception e) {
            state.getExceptionHandler().handleException("Error when executing bitmapping.", e);
        }

        return null;
    }

    private static <T> T mapBits(BigInteger value, int from, int to, List<T> values) {
        int exponent = to - from + 1;
        int length = (int) Math.pow(2, exponent);

        BigInteger rest = value.shiftRight(from);
        int index = rest.and(BigInteger.valueOf(length - (long) 1)).intValue();

        return values.get(index);
    }
}
