package au.csiro.data61.aap.program;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityArray;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.util.MethodResult;

/**
 * BlockScope
 */
public class BlockScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES;
    public static final Variable EARLIEST = new Variable(SolidityInteger.DEFAULT_INSTANCE, "earliest", VariableCategory.SCOPE_VARIABLE, 0);
    public static final Variable CURRENT = new Variable(SolidityString.DEFAULT_INSTANCE, "current", VariableCategory.SCOPE_VARIABLE, "current");
    public static final Variable PENDING = new Variable(SolidityString.DEFAULT_INSTANCE, "pending", VariableCategory.SCOPE_VARIABLE, "pending");

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
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.timestamp");
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "block.transactions");
        addVariable(DEFAULT_VARIABLES, new SolidityArray(SolidityBytes.DEFAULT_INSTANCE), "block.uncles");
    }

    private final Variable fromBlockNumber;
    private final Variable toBlockNumber;

    public BlockScope(Variable fromBlockNumber, Variable toBlockNumber) {
        super(DEFAULT_VARIABLES);

        assert isValidBlockNumberVariable(fromBlockNumber);
        assert isValidBlockNumberVariable(toBlockNumber);
        this.fromBlockNumber = fromBlockNumber;
        this.toBlockNumber = toBlockNumber;
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
    public MethodResult<Void> execute(ProgramState state) {
        BigInteger startBlock = null;
        Predicate<BigInteger> stopCriterion = null;

        // TODO: if pending, set export mode to block!

        try {
            startBlock = this.getBlockNumber(state, this.fromBlockNumber);
            stopCriterion = this.createStopCriterion(state);
        }
        catch (IOException ex) {
            final String message = this.createInitializationErrorMessage();
            state.reportException(message, ex);
            return MethodResult.ofError(message, ex);
        }

        return this.loopThroughBlocks(state, startBlock, stopCriterion);
    }

    private MethodResult<Void> loopThroughBlocks(ProgramState state, BigInteger startBlock, Predicate<BigInteger> stopCriterion) {
        BigInteger currentBlock = startBlock;
        while (!stopCriterion.test(currentBlock)) {
            try {
                final EthereumBlock block = state.getEthereumClient().queryBlockData(currentBlock);
                final MethodResult<Void> result = this.processBlock(state, block);
                if (!result.isSuccessful() && !state.continueAfterException()) {
                    return result;
                }
            }
            catch(Exception ex) {
                final String message = String.format("Error processing block %s.", currentBlock);
                state.reportException(message, ex);
                if (!state.continueAfterException()) {
                    return MethodResult.ofError(message, ex);
                }
            }

            currentBlock = currentBlock.add(BigInteger.ONE);
        }
        return MethodResult.ofResult();
    }

    private MethodResult<Void> processBlock(ProgramState state, EthereumBlock block) {
        this.setBlockVariables(block);

        for (int i = 0; i < this.instructionCount(); i++) {
            final MethodResult<Void> result = this.getInstruction(i).execute(state);
            if (!result.isSuccessful()) {
                return result;
            }
        }

        return MethodResult.ofResult();
    }

    private void setBlockVariables(EthereumBlock block) {
    }

    private String createInitializationErrorMessage() {
        return String.format(
            "Error during initialization of block scope with parameters: from='%s' and to='%s'.",
            this.fromBlockNumber.getValue(),
            this.toBlockNumber.getValue() 
        );
    }

    private BigInteger getBlockNumber(ProgramState state, Variable variable) throws IOException {
        if (variable == EARLIEST) {
            return BigInteger.ZERO;
        }
        else if (variable == CURRENT) {
            return state.getEthereumClient().queryBlockNumber();
        }
        else if (variable == PENDING) {
            throw new UnsupportedOperationException("PENDING cannot be converted into a number");
        }
        else {
            return (BigInteger)variable.getValue();
        }
    }

    private Predicate<BigInteger> createStopCriterion(ProgramState state) throws IOException {
        if (this.toBlockNumber == PENDING) {
            return number -> true;
        }

        final BigInteger toNumber = this.getBlockNumber(state, this.toBlockNumber);
        return number -> number.equals(toNumber);
    }

    public static boolean isValidBlockNumberVariable(Variable variable) {
        return     variable != null 
                && (
                      variable == PENDING 
                   || variable == EARLIEST 
                   || variable == CURRENT 
                   || SolidityInteger.DEFAULT_INSTANCE.conceptuallyEquals(variable.getType())
                )
        ;
    }

}