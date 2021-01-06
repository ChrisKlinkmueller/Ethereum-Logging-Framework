package blf.library.compression;

import java.math.BigInteger;
import java.util.List;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * BitMapping
 */
public class BitMapping {
    public static final String METHOD_NAME = "mapBits";

    public static Object mapBitsToBool(Object[] parameters, ProgramState state) throws ProgramException {
        return mapBits(parameters, Boolean.class);
    }

    public static Object mapBitsToInt(Object[] parameters, ProgramState state) throws ProgramException {
        return mapBits(parameters, BigInteger.class);
    }

    public static Object mapBitsToString(Object[] parameters, ProgramState state) throws ProgramException {
        return mapBits(parameters, String.class);
    }

    @SuppressWarnings("all")
    private static <T> T mapBits(Object[] parameters, Class<T> cl) throws ProgramException {
        try {
            final BigInteger value = (BigInteger) parameters[0];
            final int from = ((BigInteger) parameters[1]).intValue();
            final int to = ((BigInteger) parameters[2]).intValue();
            final List<T> values = (List<T>) parameters[3];
            return mapBits(value, from, to, values);
        } catch (Throwable cause) {
            throw new ProgramException("Error when executing bitmapping.", cause);
        }
    }

    private static <T> T mapBits(BigInteger value, int from, int to, List<T> values) {
        int exponent = to - from + 1;
        int length = (int) Math.pow(2, exponent);

        BigInteger rest = value.shiftRight(from);
        int index = rest.and(BigInteger.valueOf(length - (long) 1)).intValue();
        return values.get(index);
    }
}
