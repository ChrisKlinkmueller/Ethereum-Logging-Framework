package blf.blockchains.template;

import blf.core.values.BlockchainVariables;
import blf.core.values.ValueAccessor;
import blf.core.values.Variable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TemplateVariables here you can define all the variables that are specific to your blockchain.
 */
public class TemplateVariables implements BlockchainVariables {

    final static HashSet<Variable> blockVariables = new HashSet<>();
    final static HashSet<Variable> transactionVariables = new HashSet<>();
    final static HashSet<Variable> logEntryVariables = new HashSet<>();

    @Override
    public ValueAccessor currentBlockNumberAccessor() {
        return state -> ((TemplateProgramState) state).getCurrentBlockNumber();
    }

    @Override
    public Map<String, String> getBlockVariableNamesAndTypes() {
        return getVariableNamesAndType(blockVariables);
    }

    @Override
    public Map<String, String> getTransactionVariableNamesAndTypes() {
        return getVariableNamesAndType(transactionVariables);
    }

    @Override
    public Map<String, String> getLogEntryVariableNamesAndTypes() {
        return getVariableNamesAndType(logEntryVariables);
    }

    private Map<String, String> getVariableNamesAndType(Set<Variable> variables) {
        return variables.stream().collect(Collectors.toMap(Variable::getName, Variable::getType));
    }

    public static boolean isBlockVariable(String name) {
        return existsVariable(name, blockVariables.stream());
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
        return Stream.concat(blockVariables.stream(), transactionVariables.stream());
    }

}
