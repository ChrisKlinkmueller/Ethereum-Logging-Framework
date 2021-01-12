package blf.blockchains.ethereum.variables;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;

/**
 * DataSourceAccessors
 */
public class EthereumVariables {

    public static ValueAccessor currentBlockNumberAccessor() {
        return state -> {
            try {
                return ((EthereumProgramState) state).getReader().getClient().queryBlockNumber();
            } catch (final Throwable error) {
                throw new ProgramException("Error when retrieving the current block number.", error);
            }
        };
    }

    public static boolean isEthereumVariable(String name) {
        return existsVariable(
            name,
            Stream.concat(
                EthereumBlockVariables.BLOCK_VARIABLES.stream(),
                Stream.concat(
                    EthereumTransactionVariables.TRANSACTION_VARIABLES.stream(),
                    EthereumLogEntryVariables.LOG_ENTRY_VARIABLES.stream()
                )
            )
        );
    }

    public static Map<String, String> getBlockVariableNamesAndTypes() {
        return getVariableNamesAndType(EthereumBlockVariables.BLOCK_VARIABLES);
    }

    public static Map<String, String> getTransactionVariableNamesAndTypes() {
        return getVariableNamesAndType(EthereumTransactionVariables.TRANSACTION_VARIABLES);
    }

    public static Map<String, String> getLogEntryVariableNamesAndTypes() {
        return getVariableNamesAndType(EthereumLogEntryVariables.LOG_ENTRY_VARIABLES);
    }

    private static Map<String, String> getVariableNamesAndType(Set<EthereumVariable> variables) {
        return variables.stream().collect(Collectors.toMap(EthereumVariable::getName, EthereumVariable::getType));
    }

    public static boolean isBlockVariable(String name) {
        return existsVariable(name, EthereumBlockVariables.BLOCK_VARIABLES.stream());
    }

    public static boolean isTransactionVariable(String name) {
        return existsVariable(
            name,
            Stream.concat(EthereumBlockVariables.BLOCK_VARIABLES.stream(), EthereumTransactionVariables.TRANSACTION_VARIABLES.stream())
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

    private static <T> T findVariable(final String name, final Function<EthereumVariable, T> mapper) {
        return variableStream().filter(variable -> variable.hasName(name)).map(mapper::apply).findFirst().orElse(null);
    }

    private static Stream<EthereumVariable> variableStream() {
        return Stream.concat(
            EthereumBlockVariables.BLOCK_VARIABLES.stream(),
            Stream.concat(
                EthereumTransactionVariables.TRANSACTION_VARIABLES.stream(),
                EthereumLogEntryVariables.LOG_ENTRY_VARIABLES.stream()
            )
        );
    }

}
