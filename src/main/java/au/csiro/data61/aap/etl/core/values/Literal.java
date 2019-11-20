package au.csiro.data61.aap.etl.core.values;

import java.math.BigInteger;
import java.util.List;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * Literal
 */
public class Literal implements ValueAccessor {
    private final Object value;

    private Literal(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue(ProgramState state) throws ProgramException {
        return this.value;
    }

    public static Literal addressLiteral(List<String> addresses) {
        return new Literal(addresses);
    }

    public static Literal stringLiteral(String value) {
        return new Literal(value);
    }

    public static Literal integerLiteral(long value) {
        return new Literal(BigInteger.valueOf(value));
    }

    public static Literal integerLiteral(String value) {
        return value == null ? new Literal(null) : new Literal(new BigInteger(value));
    }

    
}