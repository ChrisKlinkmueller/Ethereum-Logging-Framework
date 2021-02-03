package blf.core.values;

import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;

/**
 * ValueMutator
 */
@FunctionalInterface
public interface ValueMutator {
    static ValueMutator createVariableMutator(@NonNull String name) {
        return (value, state) -> state.getValueStore().setValue(name, value);
    }

    void setValue(Object value, ProgramState state);
}
