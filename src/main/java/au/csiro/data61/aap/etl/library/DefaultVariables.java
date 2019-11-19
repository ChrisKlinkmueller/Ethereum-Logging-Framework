package au.csiro.data61.aap.etl.library;

import java.util.function.Function;

import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import au.csiro.data61.aap.etl.core.DataSource;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ValueMutator;
import au.csiro.data61.aap.rpc.EthereumBlock;

/**
 * DefaultVariables
 */
public class DefaultVariables {
      

    private static <T> Instruction createBlockVariableHandler(
        Function<DataSource, T> entityAccessor, 
        Function<T, Object> entityValueAccessor,
        ValueMutator storageValueMutator
    ) {
        assert entityAccessor != null;
        assert entityValueAccessor != null;
        assert storageValueMutator != null;
        return (state) -> {
            final T entity = entityAccessor.apply(state.getDataSource());
            final Object value = entityValueAccessor.apply(entity);
            storageValueMutator.setValue(value, state);
        };
    }
}