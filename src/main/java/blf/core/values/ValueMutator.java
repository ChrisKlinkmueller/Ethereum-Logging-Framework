package blf.core.values;

import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * ValueMutator
 */
@FunctionalInterface
public interface ValueMutator {
    public void setValue(Object value, ProgramState state) throws ProgramException;

    public static ValueMutator createVariableMutator(String name) {
        assert name != null;
        return (value, state) -> state.getValueStore().setValue(name, value);
    }
}
