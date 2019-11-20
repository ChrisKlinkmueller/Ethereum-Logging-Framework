package au.csiro.data61.aap.etl.core.variables;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import au.csiro.data61.aap.etl.core.readers.EthereumBlock;

/**
 * BlockVariables
 */
public class BlockVariables {
    static final Set<EthereumVariable> BLOCK_VARIABLES;

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
        BLOCK_VARIABLES = new HashSet<>();
        addBlockVariable(BLOCK_DIFFICULTY, "int", EthereumBlock::getDifficulty);
        addBlockVariable(BLOCK_EXTRA_DATA, "string", EthereumBlock::getExtraData);
        addBlockVariable(BLOCK_GAS_LIMIT, "int", EthereumBlock::getGasLimit);
        addBlockVariable(BLOCK_GAS_USED, "int", EthereumBlock::getGasUsed);
        addBlockVariable(BLOCK_HASH, "bytes", EthereumBlock::getHash);
        addBlockVariable(BLOCK_LOGS_BLOOM, "string", EthereumBlock::getLogsBloom);
        addBlockVariable(BLOCK_MINER, "address", EthereumBlock::getMiner);
        addBlockVariable(BLOCK_NONCE, "bytes", EthereumBlock::getNonce);
        addBlockVariable(BLOCK_NUMBER, "int", EthereumBlock::getNumber);
        addBlockVariable(BLOCK_PARENT_HASH, "bytes", EthereumBlock::getParentHash);
        addBlockVariable(BLOCK_RECEIPTS_ROOT, "bytes", EthereumBlock::getReceiptsRoot);
        addBlockVariable(BLOCK_SHA3_UNCLES, "bytes", EthereumBlock::getSha3uncles);
        addBlockVariable(BLOCK_SIZE, "int", EthereumBlock::getSize);
        addBlockVariable(BLOCK_STATE_ROOT, "bytes", EthereumBlock::getStateRoot);
        addBlockVariable(BLOCK_TIMESTAMP, "int", EthereumBlock::getTimestamp);
        addBlockVariable(BLOCK_TOTAL_DIFFICULTY, "int", EthereumBlock::getTotalDifficulty);
        addBlockVariable(BLOCK_TRANSACTIONS, "bytes", block -> BigInteger.valueOf(block.transactionCount()));
        addBlockVariable(BLOCK_TRANSACTION_ROOT, "int",  EthereumBlock::getTransactionsRoot);
    }

    private static void addBlockVariable(String name, String type, Function<EthereumBlock, Object> blockValueExtractor) {
        EthereumVariable.addVariable(BLOCK_VARIABLES, name, type, state -> blockValueExtractor.apply(state.getReader().getCurrentBlock()));
    }
}