package blf.core.values;

import blf.blockchains.ethereum.variables.EthereumVariables;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

/**
 * Variables
 */
public class Variables {

    public static ValueAccessor createValueAccessor(@NonNull String name) {
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

    public static ValueMutator createValueMutator(@NonNull String name) {
        return (value, state) -> state.getValueStore().setValue(name, value);
    }
}
