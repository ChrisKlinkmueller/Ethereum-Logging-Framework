package au.csiro.data61.aap.spec;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityString;
import au.csiro.data61.aap.state.ProgramState;

/**
 * BlockScope
 */
public class BlockScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES;
    public static final Variable EARLIEST = new Variable(SolidityInteger.DEFAULT_INSTANCE, "earliest",
            VariableCategory.SCOPE_VARIABLE, 0);
    public static final Variable CURRENT = new Variable(SolidityString.DEFAULT_INSTANCE, "current",
            VariableCategory.SCOPE_VARIABLE, "current");
    public static final Variable PENDING = new Variable(SolidityString.DEFAULT_INSTANCE, "pending",
            VariableCategory.SCOPE_VARIABLE, "pending");

    private final Variable fromBlockNumber;
    private final Variable toBlockNumber;

    public BlockScope(Variable fromBlockNumber, Variable toBlockNumber) {
        assert isValidBlockNumberVariable(fromBlockNumber);
        assert isValidBlockNumberVariable(toBlockNumber);
        this.fromBlockNumber = fromBlockNumber;
        this.toBlockNumber = toBlockNumber;
    }

    static {
        DEFAULT_VARIABLES = new HashSet<>();
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.number");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.hash");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.parentHash");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.nonce");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.sha3Uncles");
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, "block.logsBloom");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.transactionsRoot");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.stateRoot");
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "block.receiptsRoot");
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, "block.miner");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.difficulty");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.totalDifficulty");
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, "block.extraData");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.size");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.gasLimit");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.gasUsed");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.imestamp");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.transactions");
        addVariable(DEFAULT_VARIABLES, new SolidityArray(SolidityBytes.DEFAULT_INSTANCE), "block.uncles");
    }

    public void setEnclosingBlock(GlobalScope scope) {
        super.setEnclosingScope(scope);
    }

    public Variable getFromBlockNumber() {
        return this.fromBlockNumber;
    }

    public Variable getToBlockNumber() {
        return this.toBlockNumber;
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException();
    }

    public static boolean isValidBlockNumberVariable(Variable variable) {
        return variable != null && (variable == PENDING || variable == EARLIEST || variable == CURRENT
                || SolidityInteger.DEFAULT_INSTANCE.castableFrom(variable.getType()));
    }

    @Override
    public Stream<Variable> defaultVariableStream() {
        return DEFAULT_VARIABLES.stream();
    }
    
}