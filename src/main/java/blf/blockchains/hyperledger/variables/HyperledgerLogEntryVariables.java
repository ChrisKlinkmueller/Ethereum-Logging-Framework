package blf.blockchains.hyperledger.variables;

import blf.core.values.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * LogEntryVariables
 */
public class HyperledgerLogEntryVariables {
    static final Set<Variable> LOG_ENTRY_VARIABLES;

    static {
        LOG_ENTRY_VARIABLES = new HashSet<>();
    }

    private HyperledgerLogEntryVariables() {}
}
