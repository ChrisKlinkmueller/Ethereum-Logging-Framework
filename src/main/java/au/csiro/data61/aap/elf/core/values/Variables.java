package au.csiro.data61.aap.elf.core.values;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.values.ValueMutator;

/**
 * Variables
 */
public class Variables {

    public static ValueAccessor createValueAccessor(String name) {
        assert name != null;
        final ValueAccessor accessor = EthereumVariables.getValueAccessor(name);
        if (accessor != null) {
            return accessor;
        }

        return state -> {
            if (!state.getValueStore().containsName(name)) {
                throw new ProgramException(String.format("Variable '%s' does not exist.", name));
            }
            return state.getValueStore().getValue(name);
        };
    }

    public static ValueMutator createValueMutator(String name) {
        assert name != null;
        return (value, state) -> state.getValueStore().setValue(name, value);
    }
}
