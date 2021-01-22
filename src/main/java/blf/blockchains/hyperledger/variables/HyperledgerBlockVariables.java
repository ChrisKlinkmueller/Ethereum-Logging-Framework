package blf.blockchains.hyperledger.variables;

import blf.core.values.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * HyperledgerBlockVariables
 */
public class HyperledgerBlockVariables {
    static final Set<Variable> BLOCK_VARIABLES;

    public static final String BLOCK_NUMBER = "block.number";
    public static final String BLOCK_HASH = "block.hash";
    public static final String BLOCK_TRANSACTION_COUNT = "block.transactionCount";

    private static final String TYPE_STRING = "string";
    @SuppressWarnings("unused")
    private static final String TYPE_BYTES = "bytes";
    private static final String TYPE_INT = "int";

    private HyperledgerBlockVariables() {}

    static {
        BLOCK_VARIABLES = new HashSet<>();

        addBlockVariable(BLOCK_NUMBER, TYPE_INT);
        addBlockVariable(BLOCK_HASH, TYPE_STRING);
        addBlockVariable(BLOCK_TRANSACTION_COUNT, TYPE_INT);
    }

    private static void addBlockVariable(String name, String type) {
        BLOCK_VARIABLES.add(new Variable(name, type, state -> state.getValueStore().getValue(name)));
    }
}
