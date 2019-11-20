package au.csiro.data61.aap.etl.library.types.types;

import java.math.BigInteger;

import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * IntegerOperations
 */
public class IntegerOperations {

    public static Object add(Object[] parameters, ProgramState state) {
        assert parameters != null && parameters.length == 2;
        assert parameters[0] instanceof BigInteger && parameters[1] instanceof BigInteger;
        final BigInteger addend1 = (BigInteger)parameters[0];
        final BigInteger addend2 = (BigInteger)parameters[1];
        return addend1.add(addend2);
    }
}