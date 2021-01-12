package blf.core.values;

import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

/**
 * ValueMutator
 */
@FunctionalInterface
public interface ValueMutator {
    public void setValue(Object value, ProgramState state) throws ProgramException;

    public static ValueMutator createVariableMutator(@NonNull String name) {
        return (value, state) -> state.getValueStore().setValue(name, value);
    }
}
