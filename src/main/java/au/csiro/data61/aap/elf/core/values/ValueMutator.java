package au.csiro.data61.aap.elf.core.values;

import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

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