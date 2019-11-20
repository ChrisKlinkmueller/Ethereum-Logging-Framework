package au.csiro.data61.aap.etl.core.variables;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * DataSourceAccessors
 */
public class EthereumVariables {

    public static ValueAccessor currentBlockNumberAccessor() {
        return state -> {
            try {
                return state.getReader().getClient().queryBlockNumber();
            } catch (Throwable error) {
                throw new ProgramException("Error when retrieving the current block number.", error);
            }
        };
    }

    public static boolean existsBlockVariable(String name) {
        return existsVariable(name, BlockVariables.BLOCK_VARIABLES);
    }

    public static boolean existsTransactionVariable(String name) {
        return existsVariable(name, TransactionVariables.TRANSACTION_VARIABLES);
    }

    public static boolean existsLogEntryVariable(String name) {
        return existsVariable(name, LogEntryVariables.LOG_ENTRY_VARIABLES);
    }

    private static boolean existsVariable(String name, Set<EthereumVariable> variables) {
        return variables.stream().anyMatch(variable -> variable.hasName(name));
    }

    public static Instruction createValueCreationInstruction(String name) {
        return findVariable(name, EthereumVariable::getValueCreator);
    }

    public static Instruction createValueRemovalInstruction(String name) {
        return findVariable(name, EthereumVariable::getValueRemover);
    }

    public static TypeReference<?> getType(String name) {
        return findVariable(name, EthereumVariable::getType);
    }

    private static <T> T findVariable(String name, Function<EthereumVariable, T> mapper) {
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