package au.csiro.data61.aap.etl.core.values;

import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * ValueGetter
 */
@FunctionalInterface
public interface ValueAccessor {
    public Object getValue(ProgramState state) throws ProgramException;
    
    public static ValueAccessor createLiteralAccessor(Object value) {
        return (state) -> value;
    }

    public static ValueAccessor createVariableAccessor(String name) {
        assert name != null;
        return state -> state.getValueStore().getValue(name);
    }
}