package blf.core.values;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

/**
 * ValueGetter
 */
@FunctionalInterface
public interface ValueAccessor {
    Object getValue(ProgramState state) throws ProgramException;

    static ValueAccessor createLiteralAccessor(Object value) {
        return state -> value;
    }

    static ValueAccessor createVariableAccessor(@NonNull String name) {
        return state -> state.getValueStore().getValue(name);
    }
}
