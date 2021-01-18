package blf.blockchains.hyperledger.variables;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.values.BlockchainVariables;
import blf.core.values.ValueAccessor;
import blf.core.values.Variable;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DataSourceAccessors
 */
public class HyperledgerVariables implements BlockchainVariables {

    @Override
    public ValueAccessor currentBlockNumberAccessor() {
        return state -> ((HyperledgerProgramState) state).getCurrentBlockNumber();
    }

    @Override
    public Map<String, String> getBlockVariableNamesAndTypes() {
        return getVariableNamesAndType(HyperledgerBlockVariables.BLOCK_VARIABLES);
    }

    @Override
    public Map<String, String> getTransactionVariableNamesAndTypes() {
        return getVariableNamesAndType(HyperledgerTransactionVariables.TRANSACTION_VARIABLES);
    }

    @Override
    public Map<String, String> getLogEntryVariableNamesAndTypes() {
        return getVariableNamesAndType(HyperledgerLogEntryVariables.LOG_ENTRY_VARIABLES);
    }

    private Map<String, String> getVariableNamesAndType(Set<Variable> variables) {
        return variables.stream().collect(Collectors.toMap(Variable::getName, Variable::getType));
    }

    public static boolean isBlockVariable(String name) {
        return existsVariable(name, HyperledgerBlockVariables.BLOCK_VARIABLES.stream());
    }

    private static boolean existsVariable(final String name, final Stream<Variable> variables) {
        return variables.anyMatch(variable -> variable.hasName(name));
    }

    @Override
    public ValueAccessor getValueAccessor(String name) {
        return findVariable(name, Variable::getAccessor);
    }

    private static <T> T findVariable(final String name, final Function<Variable, T> mapper) {
        return variableStream().filter(variable -> variable.hasName(name)).map(mapper).findFirst().orElse(null);
    }

    private static Stream<Variable> variableStream() {
        return Stream.concat(
            HyperledgerBlockVariables.BLOCK_VARIABLES.stream(),
            Stream.concat(
                HyperledgerTransactionVariables.TRANSACTION_VARIABLES.stream(),
                HyperledgerLogEntryVariables.LOG_ENTRY_VARIABLES.stream()
            )
        );
    }

}
