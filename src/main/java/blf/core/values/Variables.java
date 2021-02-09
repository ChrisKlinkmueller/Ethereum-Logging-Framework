package blf.core.values;

import blf.core.exceptions.ExceptionHandler;
import io.reactivex.annotations.NonNull;

/**
 * Variables
 */
public class Variables {

    private Variables() {}

    public static ValueAccessor createValueAccessor(@NonNull String name, BlockchainVariables blockchainVariables) {
        final ValueAccessor accessor = blockchainVariables.getValueAccessor(name);
        if (accessor != null) {
            return accessor;
        }

        return state -> {
            final boolean variableExists = state.getValueStore().containsName(name);
            final Object variableValue = state.getValueStore().getValue(name);

            if (!variableExists) {
                final String errorMsg = String.format("Variable '%s' does not exist.", name);
                ExceptionHandler.getInstance().handleException(errorMsg, new Exception());

                return null;
            }

            return variableValue;
        };
    }

    public static ValueMutator createValueMutator(String name) {
        return (value, state) -> state.getValueStore().setValue(name, value);
    }
}
