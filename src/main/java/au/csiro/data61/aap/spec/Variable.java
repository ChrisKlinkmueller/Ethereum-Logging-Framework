package au.csiro.data61.aap.spec;

import java.util.HashSet;
import java.util.Set;

import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Variable
 */
public class Variable implements ValueSource {
    private final SolidityType type;
    private final String name;
    private final boolean isConstant;
    protected Object value;

    public Variable(SolidityType type, String name) {
        this(type, name, false, null);
    }

    public Variable(SolidityType type, String name, boolean isConstant, Object value) {
        assert type != null;
        assert name != null;
        assert isConstant ? value != null : true;
        this.type = type;
        this.name = name;
        this.isConstant = isConstant;
        this.value = value;
    }

    public boolean isConstant() {
        return isConstant;
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    public void setValue(Object object) {
        if (this.isConstant) {
            throw new UnsupportedOperationException("The value of a constant cannot be updated!");
        }
    }

    @Override
    public MethodResult<Object> getValue() {
        return MethodResult.ofResult(this.value);
    }

    public String getName() {
        return this.name;
    }

    public static boolean isVariableNameReserved(String variableName) {
        assert variableName != null;
        // TODO: in case of a logentry block, the log entry variables must be included!

        return RESERVED_VARIABLE_NAMES.contains(variableName);
    }

    private static final Set<String> RESERVED_VARIABLE_NAMES;

    static {
        RESERVED_VARIABLE_NAMES = new HashSet<>();

        // BLOCK VARIABLES
        RESERVED_VARIABLE_NAMES.add("block.number");
        RESERVED_VARIABLE_NAMES.add("block.hash");
        RESERVED_VARIABLE_NAMES.add("block.parentHash");
        RESERVED_VARIABLE_NAMES.add("block.nonce");
        RESERVED_VARIABLE_NAMES.add("block.sha3Uncles");
        RESERVED_VARIABLE_NAMES.add("block.logsBloom");
        RESERVED_VARIABLE_NAMES.add("block.transactionsRoot");
        RESERVED_VARIABLE_NAMES.add("block.stateRoot");
        RESERVED_VARIABLE_NAMES.add("block.receiptsRoot");
        RESERVED_VARIABLE_NAMES.add("block.miner");
        RESERVED_VARIABLE_NAMES.add("block.difficulty");
        RESERVED_VARIABLE_NAMES.add("block.totalDifficulty");
        RESERVED_VARIABLE_NAMES.add("block.extraData");
        RESERVED_VARIABLE_NAMES.add("block.size");
        RESERVED_VARIABLE_NAMES.add("block.gasLimit");
        RESERVED_VARIABLE_NAMES.add("block.gasUsed");
        RESERVED_VARIABLE_NAMES.add("block.imestamp");
        RESERVED_VARIABLE_NAMES.add("block.transactions");
        RESERVED_VARIABLE_NAMES.add("block.uncles");

        // TRANSACTION VARIABLES
        RESERVED_VARIABLE_NAMES.add("tx.blockHash");
        RESERVED_VARIABLE_NAMES.add("tx.blockNumber");
        RESERVED_VARIABLE_NAMES.add("tx.from");
        RESERVED_VARIABLE_NAMES.add("tx.gas");
        RESERVED_VARIABLE_NAMES.add("tx.gasPrice");
        RESERVED_VARIABLE_NAMES.add("tx.hash");
        RESERVED_VARIABLE_NAMES.add("tx.input");
        RESERVED_VARIABLE_NAMES.add("tx.nonce");
        RESERVED_VARIABLE_NAMES.add("tx.to");
        RESERVED_VARIABLE_NAMES.add("tx.transactionIndex");
        RESERVED_VARIABLE_NAMES.add("tx.value");
        RESERVED_VARIABLE_NAMES.add("tx.v");
        RESERVED_VARIABLE_NAMES.add("tx.r");
        RESERVED_VARIABLE_NAMES.add("tx.s");

        // LOG ENTRY VARIABLES
        RESERVED_VARIABLE_NAMES.add("log.removed");
        RESERVED_VARIABLE_NAMES.add("log.logIndex");
        RESERVED_VARIABLE_NAMES.add("log.transactionIndex");
        RESERVED_VARIABLE_NAMES.add("log.transactionHash");
        RESERVED_VARIABLE_NAMES.add("log.blockHash");
        RESERVED_VARIABLE_NAMES.add("log.blockNumber");
        RESERVED_VARIABLE_NAMES.add("log.address");
    }
}