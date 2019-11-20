package au.csiro.data61.aap.etl.core.filters;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
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

    public static boolean existsBlockVariable(final String name) {
        return existsVariable(name, BlockVariables.BLOCK_VARIABLES);
    }

    public static boolean existsTransactionVariable(final String name) {
        return existsVariable(name, TransactionVariables.TRANSACTION_VARIABLES);
    }

    public static boolean existsLogEntryVariable(final String name) {
        return existsVariable(name, LogEntryVariables.LOG_ENTRY_VARIABLES);
    }

    private static boolean existsVariable(final String name, final Set<EthereumVariable> variables) {
        return variables.stream().anyMatch(variable -> variable.hasName(name));
    }

    public static List<Instruction> getBlockValueCreators() {
        return mapVariables(BlockVariables.BLOCK_VARIABLES, EthereumVariable::getValueCreator);
    }

    public static List<Instruction> getTransactionValueCreators() {
        return mapVariables(TransactionVariables.TRANSACTION_VARIABLES, EthereumVariable::getValueCreator);
    }

    public static List<Instruction> getLogEntryValueCreators() {
        return mapVariables(LogEntryVariables.LOG_ENTRY_VARIABLES, EthereumVariable::getValueCreator);
    }

    public static List<Instruction> getBlockValueRemovers() {
        return mapVariables(BlockVariables.BLOCK_VARIABLES, EthereumVariable::getValueRemover);
    }

    public static List<Instruction> getTransactionRemovers() {
        return mapVariables(TransactionVariables.TRANSACTION_VARIABLES, EthereumVariable::getValueRemover);
    }

    public static List<Instruction> getLogEntryRemovers() {
        return mapVariables(LogEntryVariables.LOG_ENTRY_VARIABLES, EthereumVariable::getValueRemover);
    }

    private static <T> List<T> mapVariables(Set<EthereumVariable> variables, final Function<EthereumVariable, T> mapper) {
        return variables.stream()
            .map(variable -> mapper.apply(variable))
            .collect(Collectors.toList());
    }

    public static Instruction createValueCreationInstruction(final String name) {
        return findVariable(name, EthereumVariable::getValueCreator);
    }

    public static Instruction createValueRemovalInstruction(final String name) {
        return findVariable(name, EthereumVariable::getValueRemover);
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