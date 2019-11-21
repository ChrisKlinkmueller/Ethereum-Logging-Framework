package au.csiro.data61.aap.etl.core.values;

import java.util.function.Function;
import java.util.stream.Stream;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * DataSourceAccessors
 */
public class EthereumVariables {

    public static ValueAccessor currentBlockNumberAccessor() {
        return state -> {
            try {
                return state.getReader().getClient().queryBlockNumber();
            } catch (final Throwable error) {
                throw new ProgramException("Error when retrieving the current block number.", error);
            }
        };
    }

    public static boolean isEthereumVariable(String name) {
        return existsVariable(
            name, 
            Stream.concat(
                BlockVariables.BLOCK_VARIABLES.stream(), 
                Stream.concat(
                    TransactionVariables.TRANSACTION_VARIABLES.stream(), 
                    LogEntryVariables.LOG_ENTRY_VARIABLES.stream()
                )
            )
        );
    }

    public static boolean isBlockVariable(String name) {
        return existsVariable(name, BlockVariables.BLOCK_VARIABLES.stream());
    }

    public static boolean isTransactionVariable(String name) {
        return existsVariable(
            name, 
            Stream.concat(
                BlockVariables.BLOCK_VARIABLES.stream(), 
                TransactionVariables.TRANSACTION_VARIABLES.stream()
            )
        );
    }

    public static boolean isLogEntryVariable(String name) {
        return isEthereumVariable(name);
    }

    private static boolean existsVariable(final String name, final Stream<EthereumVariable> variables) {
        return variables.anyMatch(variable -> variable.hasName(name));
    }

    public static ValueAccessor getValueAccessor(String name) {
        return findVariable(name, EthereumVariable::getAccessor);
    }

    public static TypeReference<?> getType(final String name) {
        return findVariable(name, EthereumVariable::getType);
    }

    private static <T> T findVariable(final String name, final Function<EthereumVariable, T> mapper) {
        return variableStream()
            .filter(variable -> variable.hasName(name))
            .map(variable -> mapper.apply(variable))
            .findFirst().orElse(null);
    }

    private static Stream<EthereumVariable> variableStream() {
        return Stream.concat(
            BlockVariables.BLOCK_VARIABLES.stream(),
            Stream.concat(
                TransactionVariables.TRANSACTION_VARIABLES.stream(),
                LogEntryVariables.LOG_ENTRY_VARIABLES.stream()
            )
        );
    }
    
}