package blf.blockchains.hyperledger.variables;

import blf.blockchains.ethereum.reader.EthereumLogEntry;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.values.Variable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * LogEntryVariables
 */
public abstract class HyperledgerLogEntryVariables {
    static final Set<Variable> LOG_ENTRY_VARIABLES;

    public static final String LOG_REMOVED = "log.removed";
    public static final String LOG_INDEX = "log.logIndex";
    public static final String LOG_ADDRESS = "log.address";

    static {
        LOG_ENTRY_VARIABLES = new HashSet<>();
        addLogEntryVariable(LOG_REMOVED, "bool", EthereumLogEntry::isRemoved);
        addLogEntryVariable(LOG_INDEX, "int", EthereumLogEntry::getLogIndex);
        addLogEntryVariable(LOG_ADDRESS, "address", EthereumLogEntry::getAddress);
    }

    private HyperledgerLogEntryVariables() {}

    private static void addLogEntryVariable(String name, String type, Function<EthereumLogEntry, Object> blockValueExtractor) {
        Variable.addVariable(
            LOG_ENTRY_VARIABLES,
            name,
            type,
            state -> blockValueExtractor.apply(((EthereumProgramState) state).getReader().getCurrentLogEntry())
        );
    }
}
