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

    public static final String BLOCK_NUMBER = "block.number";
    public static final String BLOCK_HASH = "block.hash";
    public static final String BLOCK_PARENT_HASH = "block.parentHash";
    public static final String BLOCK_NONCE = "block.nonce";
    public static final String BLOCK_SHA3_UNCLES = "block.sha3Uncles";
    public static final String BLOCK_LOGS_BLOOM = "block.logsBloom";
    public static final String BLOCK_TRANSACTION_ROOT = "block.transactionsRoot";
    public static final String BLOCK_STATE_ROOT = "block.stateRoot";
    public static final String BLOCK_RECEIPT_ROOT = "block.receiptsRoot";
    public static final String BLOCK_MINER = "block.miner";
    public static final String BLOCK_DIFFICULTY = "block.difficulty";
    public static final String BLOCK_TOTAL_DIFFICULTY = "block.totalDifficulty";
    public static final String BLOCK_EXTRA_DATA = "block.extraData";
    public static final String BLOCK_SIZE = "block.size";
    public static final String BLOCK_GAS_LIMIT = "block.gasLimit";
    public static final String BLOCK_GAS_USED = "block.gasUsed";
    public static final String BLOCK_TIMESTAMP = "block.timestamp";
    public static final String BLOCK_TRANSACTIONS = "block.transactions";
    public static final String BLOCK_UNCLES = "block.uncles";

    static {
        DEFAULT_VARIABLES = new HashSet<>();
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_NUMBER);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_HASH);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_PARENT_HASH);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_NONCE);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_SHA3_UNCLES);
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, BLOCK_LOGS_BLOOM);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_TRANSACTION_ROOT);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_STATE_ROOT);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_RECEIPT_ROOT);
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, BLOCK_MINER);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_DIFFICULTY);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TOTAL_DIFFICULTY);
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, BLOCK_EXTRA_DATA);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_SIZE);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_GAS_LIMIT);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_GAS_USED);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TIMESTAMP);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TRANSACTIONS);
        addVariable(DEFAULT_VARIABLES, new SolidityArray(SolidityBytes.DEFAULT_INSTANCE), BLOCK_UNCLES);
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
        this.setBlockVariables(state, block);

        for (int i = 0; i < this.instructionCount(); i++) {
            final MethodResult<Void> result = this.getInstruction(i).execute(state);
            if (!result.isSuccessful()) {
                return result;
            }
        }

        return MethodResult.ofResult();
    }

    private void setBlockVariables(ProgramState state, EthereumBlock block) {
        state.setCurrentBlock(block);

        //this.setBlockVariableValue(BLOCK_DIFFICULTY, block.getDifficulty(), ValueCasts::stringToInteger);
        //this.setBlockVariableValue(BLOCK_EXTRA_DATA, block.getExtraData(), null);
        //this.setBlockVariableValue(BLOCK_GAS_LIMIT, block.getGasLimit(), ValueCasts::stringToInteger);
        //this.setBlockVariableValue(BLOCK_GAS_USED, block.getGasUsed(), ValueCasts::stringToInteger);        
        //this.setBlockVariableValue(BLOCK_HASH, block.getHash(), null);  
        //this.setBlockVariableValue(BLOCK_LOGS_BLOOM, block.getLogsBloom(), null);  
        //this.setBlockVariableValue(BLOCK_MINER, block.getMiner(), null);
        //this.setBlockVariableValue(BLOCK_NONCE, block.getNonce(), null);
        //this.setBlockVariableValue(BLOCK_NUMBER, block.getNumber(), ValueCasts::stringToInteger);
        /*
    }

    private void setBlockVariableValue(String variableName, Object value, Function<Object, Object> cast) {
        assert value != null;
        final Variable variable = this.getVariable(variableName);
        assert variable != null;
        variable.setValue(cast == null ? value : cast.apply(value));*/
    }
}