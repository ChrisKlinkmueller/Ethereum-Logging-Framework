package blf.core.values;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

/**
 * ValueGetter
 */
@FunctionalInterface
public interface ValueAccessor {
    public Object getValue(ProgramState state) throws ProgramException;

    public static ValueAccessor createLiteralAccessor(Object value) {
        return state -> value;
    }

    public static ValueAccessor createVariableAccessor(@NonNull String name) {
        return state -> state.getValueStore().getValue(name);
    }
}
