package blf.core.values;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import blf.blockchains.ethereum.reader.EthereumLogEntry;
import blf.blockchains.ethereum.state.EthereumProgramState;

/**
 * LogEntryVariables
 */
public abstract class EthereumLogEntryVariables {
    static final Set<EthereumVariable> LOG_ENTRY_VARIABLES;

    public static final String LOG_REMOVED = "log.removed";
    public static final String LOG_INDEX = "log.logIndex";
    public static final String LOG_ADDRESS = "log.address";

    static {
        LOG_ENTRY_VARIABLES = new HashSet<>();
        addLogEntryVariable(LOG_REMOVED, "bool", EthereumLogEntry::isRemoved);
        addLogEntryVariable(LOG_INDEX, "int", EthereumLogEntry::getLogIndex);
        addLogEntryVariable(LOG_ADDRESS, "address", EthereumLogEntry::getAddress);
    }

    private EthereumLogEntryVariables() {}

    private static void addLogEntryVariable(String name, String type, Function<EthereumLogEntry, Object> blockValueExtractor) {
        EthereumVariable.addVariable(
            LOG_ENTRY_VARIABLES,
            name,
            type,
            state -> blockValueExtractor.apply(((EthereumProgramState) state).getReader().getCurrentLogEntry())
        );
    }
}
