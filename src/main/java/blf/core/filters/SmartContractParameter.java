package blf.core.filters;

import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

/**
 * SmartContractParameter
 */
public class SmartContractParameter extends Parameter {
    private final ValueAccessor accessor;

    public SmartContractParameter(String solType, String name, @NonNull ValueAccessor accessor) {
        super(solType, name);
        this.accessor = accessor;
    }

    public ValueAccessor getAccessor() {
        return this.accessor;
    }
}
