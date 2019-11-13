package au.csiro.data61.aap.program;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.suppliers.VariableCategory;
import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.ValueCasts;
import au.csiro.data61.aap.program.types.ValueCasts.ValueCastException;
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
    public static final String BLOCK_RECEIPTS_ROOT = "block.receiptsRoot";
    public static final String BLOCK_MINER = "block.miner";
    public static final String BLOCK_DIFFICULTY = "block.difficulty";
    public static final String BLOCK_TOTAL_DIFFICULTY = "block.totalDifficulty";
    public static final String BLOCK_EXTRA_DATA = "block.extraData";
    public static final String BLOCK_SIZE = "block.size";
    public static final String BLOCK_GAS_LIMIT = "block.gasLimit";
    public static final String BLOCK_GAS_USED = "block.gasUsed";
    public static final String BLOCK_TIMESTAMP = "block.timestamp";
    public static final String BLOCK_TRANSACTIONS = "block.transactions";

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
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_RECEIPTS_ROOT);
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, BLOCK_MINER);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_DIFFICULTY);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TOTAL_DIFFICULTY);
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, BLOCK_EXTRA_DATA);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_SIZE);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_GAS_LIMIT);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_GAS_USED);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TIMESTAMP);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TRANSACTIONS);
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
        try {
            extractValues(VALUE_EXTRACTORS, block, this);
        }
        catch (ValueCastException ex) {
            return MethodResult.ofError(String.format("Error when retrieving data for block %s.", block.getNumber()), ex);
        }

        for (int i = 0; i < this.instructionCount(); i++) {
            final MethodResult<Void> result = this.getInstruction(i).execute(state);
            if (!result.isSuccessful()) {
                return result;
            }
        }

        return MethodResult.ofResult();
    }

    private static List<ValueExtractor<EthereumBlock>> VALUE_EXTRACTORS = List.of(
        new ValueExtractor<EthereumBlock>(BLOCK_DIFFICULTY, EthereumBlock::getDifficulty, ValueCasts::stringToInteger),
        new ValueExtractor<EthereumBlock>(BLOCK_EXTRA_DATA, EthereumBlock::getExtraData),
        new ValueExtractor<EthereumBlock>(BLOCK_GAS_LIMIT, EthereumBlock::getGasLimit, ValueCasts::stringToInteger),
        new ValueExtractor<EthereumBlock>(BLOCK_GAS_USED, EthereumBlock::getGasUsed, ValueCasts::stringToInteger),        
        new ValueExtractor<EthereumBlock>(BLOCK_HASH, EthereumBlock::getHash, ValueCasts::stringToBytes),  
        new ValueExtractor<EthereumBlock>(BLOCK_LOGS_BLOOM, EthereumBlock::getLogsBloom),  
        new ValueExtractor<EthereumBlock>(BLOCK_MINER, EthereumBlock::getMiner, ValueCasts::stringToAddress),
        new ValueExtractor<EthereumBlock>(BLOCK_NONCE, EthereumBlock::getNonce, ValueCasts::stringToBytes),
        new ValueExtractor<EthereumBlock>(BLOCK_NUMBER, EthereumBlock::getNumber, ValueCasts::stringToInteger),
        new ValueExtractor<EthereumBlock>(BLOCK_PARENT_HASH, EthereumBlock::getParentHash, ValueCasts::stringToBytes),
        new ValueExtractor<EthereumBlock>(BLOCK_RECEIPTS_ROOT, EthereumBlock::getReceiptsRoot, ValueCasts::stringToBytes),
        new ValueExtractor<EthereumBlock>(BLOCK_SHA3_UNCLES, EthereumBlock::getSha3uncles, ValueCasts::stringToBytes),
        new ValueExtractor<EthereumBlock>(BLOCK_SIZE, EthereumBlock::getSize, ValueCasts::stringToInteger),
        new ValueExtractor<EthereumBlock>(BLOCK_STATE_ROOT, EthereumBlock::getStateRoot, ValueCasts::stringToBytes),
        new ValueExtractor<EthereumBlock>(BLOCK_TIMESTAMP, EthereumBlock::getTimestamp, ValueCasts::stringToInteger),
        new ValueExtractor<EthereumBlock>(BLOCK_TOTAL_DIFFICULTY, EthereumBlock::getTotalDifficulty, ValueCasts::stringToInteger),
        new ValueExtractor<EthereumBlock>(BLOCK_TRANSACTIONS, block -> BigInteger.valueOf(block.transactionCount())),
        new ValueExtractor<EthereumBlock>(BLOCK_TRANSACTION_ROOT, EthereumBlock::getTransactionsRoot, ValueCasts::stringToBytes)
    );

}