package au.csiro.data61.aap.program;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.suppliers.BlockchainVariable;
import au.csiro.data61.aap.program.suppliers.Literal;
import au.csiro.data61.aap.program.suppliers.MethodCall;
import au.csiro.data61.aap.program.suppliers.ValueSupplier;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.ValueCasts;
import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.util.MethodResult;

/**
 * BlockScope
 */
public class BlockScope extends Scope {
    public static final Set<Variable> DEFAULT_VARIABLES;
    public static final ValueSupplier EARLIEST = new Literal(SolidityInteger.DEFAULT_INSTANCE, 0);
    public static final ValueSupplier CURRENT = new Literal(SolidityString.DEFAULT_INSTANCE, "current");
    public static final ValueSupplier CONTINUOUS = new Literal(SolidityString.DEFAULT_INSTANCE, "pending");

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
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_DIFFICULTY, EthereumBlock::getDifficulty, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, BLOCK_EXTRA_DATA, EthereumBlock::getExtraData, null);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_GAS_LIMIT, EthereumBlock::getGasLimit, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_GAS_USED, EthereumBlock::getGasUsed, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_HASH, EthereumBlock::getHash, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, BLOCK_LOGS_BLOOM, EthereumBlock::getLogsBloom, null);
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, BLOCK_MINER, EthereumBlock::getMiner, ValueCasts::stringToAddress);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_NONCE, EthereumBlock::getNonce, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_NUMBER, EthereumBlock::getNumber, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_PARENT_HASH, EthereumBlock::getParentHash, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_RECEIPTS_ROOT, EthereumBlock::getReceiptsRoot, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_SHA3_UNCLES, EthereumBlock::getSha3uncles, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_SIZE, EthereumBlock::getSize, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_STATE_ROOT, EthereumBlock::getStateRoot, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TIMESTAMP, EthereumBlock::getTimestamp, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TOTAL_DIFFICULTY, EthereumBlock::getTotalDifficulty, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, BLOCK_TRANSACTIONS, (EthereumBlock block) -> BigInteger.valueOf(block.transactionCount()), null);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, BLOCK_TRANSACTION_ROOT, EthereumBlock::getTransactionsRoot, ValueCasts::stringToBytes);
    }

    public static boolean isValidBlockNumberVariable(ValueSupplier variable) {
        if (variable == null || variable instanceof MethodCall) {
            return false;
        }        
        return    variable == CONTINUOUS || variable == EARLIEST || variable == CURRENT
               || SolidityInteger.DEFAULT_INSTANCE.conceptuallyEquals(variable.getType());
    }

    private final ValueSupplier fromBlockNumber;
    private final ValueSupplier toBlockNumber;
    private final List<BlockchainVariable<EthereumBlock>> variables;

    @SuppressWarnings("unchecked")
    public BlockScope(ValueSupplier fromBlockNumber, ValueSupplier toBlockNumber) {
        assert isValidBlockNumberVariable(fromBlockNumber);
        assert isValidBlockNumberVariable(toBlockNumber);
        this.fromBlockNumber = fromBlockNumber;
        this.toBlockNumber = toBlockNumber;
        this.variables = DEFAULT_VARIABLES.stream()
            .map(variable -> new BlockchainVariable<EthereumBlock>((BlockchainVariable<EthereumBlock>)variable))
            .collect(Collectors.toList());
    }

    @Override
    public Stream<? extends Variable> variableStream() {
        return this.variables.stream();
    }

    public void setEnclosingBlock(GlobalScope scope) {
        super.setEnclosingScope(scope);
    }

    public ValueSupplier getFromBlockNumber() {
        return this.fromBlockNumber;
    }

    public ValueSupplier getToBlockNumber() {
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
        } catch (Throwable cause) {
            final String message = "Error during initialization of block scope.";
            state.reportException(message, cause);
            return MethodResult.ofError(message, cause);
        }

        return this.loopThroughBlocks(state, startBlock, stopCriterion);
    }

    private BigInteger getBlockNumber(ProgramState state, ValueSupplier variable) throws Throwable {
        if (variable == EARLIEST) {
            return BigInteger.ZERO;
        } else if (variable == CURRENT) {
            return state.getEthereumClient().queryBlockNumber();
        } else if (variable == CONTINUOUS) {
            throw new UnsupportedOperationException("CONTINUOUS cannot be converted into a number");
        } else {
            return (BigInteger)variable.getValue();
        }
    }

    private Predicate<BigInteger> createStopCriterion(ProgramState state) throws Throwable {
        if (this.toBlockNumber == CONTINUOUS) {
            return number -> true;
        }

        final BigInteger toNumber = this.getBlockNumber(state, this.toBlockNumber);
        return number -> number.equals(toNumber);
    }

    private MethodResult<Void> loopThroughBlocks(ProgramState state, BigInteger startBlock,
            Predicate<BigInteger> stopCriterion) {
        BigInteger currentBlock = startBlock;
        while (!stopCriterion.test(currentBlock)) {
            try {
                final EthereumBlock block = state.getEthereumClient().queryBlockData(currentBlock);
                final MethodResult<Void> result = this.processBlock(state, block);
                if (!result.isSuccessful() && !state.continueAfterException()) {
                    return result;
                }
            } catch (Throwable ex) {
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
            this.variables.stream().forEach(variable -> variable.setValueProvider(block));
        } catch (Throwable cause) {
            return MethodResult.ofError(String.format("Error when retrieving data for block %s.", block.getNumber()), cause);
        }

        for (int i = 0; i < this.instructionCount(); i++) {
            final MethodResult<Void> result = this.getInstruction(i).execute(state);
            if (!result.isSuccessful()) {
                return result;
            }
        }

        return MethodResult.ofResult();
    }
}