package blf.blockchains.ethereum.classes;

import blf.core.parameters.Parameter;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

/**
 * SmartContractParameter
 */
public class EthereumSmartContractParameter extends Parameter {
    private final ValueAccessor accessor;

    public EthereumSmartContractParameter(String solType, String name, @NonNull ValueAccessor accessor) {
        super(solType, name);
        this.accessor = accessor;
    }

    public ValueAccessor getAccessor() {
        return this.accessor;
    }
}
