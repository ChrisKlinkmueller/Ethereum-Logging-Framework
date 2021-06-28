package au.csiro.data61.aap.elf.core.values;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import au.csiro.data61.aap.elf.core.readers.EthereumLogEntry;
import au.csiro.data61.aap.elf.core.values.ValueAccessor.ProgramFunction;

/**
 * LogEntryVariables
 */
public class LogEntryVariables {
    static final Set<EthereumVariable> LOG_ENTRY_VARIABLES;

    public static String LOG_REMOVED = "log.removed";
    public static String LOG_INDEX = "log.logIndex";
    public static String LOG_ADDRESS = "log.address";

    static {
        LOG_ENTRY_VARIABLES = new HashSet<>();
        addLogEntryVariable(LOG_REMOVED, "bool", EthereumLogEntry::isRemoved);
        addLogEntryVariable(LOG_INDEX, "int", EthereumLogEntry::getLogIndex);
        addLogEntryVariable(LOG_ADDRESS, "address", EthereumLogEntry::getAddress);
    }

    private static void addLogEntryVariable(String name, String type, Function<EthereumLogEntry, Object> blockValueExtractor) {
        final ProgramFunction function = state -> blockValueExtractor.apply(state.getReader().getCurrentLogEntry());
        EthereumVariable.addVariable(LOG_ENTRY_VARIABLES, name, type, ValueAccessor.createFunctionAccessor(function));
    }
}
