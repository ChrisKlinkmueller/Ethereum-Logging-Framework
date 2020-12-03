package au.csiro.data61.aap.elf.core.values;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import au.csiro.data61.aap.elf.core.readers.EthereumBlock;

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
    public static final String BLOCK_TRANSACTIONS = "block.transactionCount";

    private static final String TYPE_STRING = "string";
    private static final String TYPE_BYTES = "bytes";

    static {
        BLOCK_VARIABLES = new HashSet<>();
        addBlockVariable(BLOCK_DIFFICULTY, "int", EthereumBlock::getDifficulty);
        addBlockVariable(BLOCK_EXTRA_DATA, TYPE_STRING, EthereumBlock::getExtraData);
        addBlockVariable(BLOCK_GAS_LIMIT, "int", EthereumBlock::getGasLimit);
        addBlockVariable(BLOCK_GAS_USED, "int", EthereumBlock::getGasUsed);
        addBlockVariable(BLOCK_HASH, TYPE_BYTES, EthereumBlock::getHash);
        addBlockVariable(BLOCK_LOGS_BLOOM, TYPE_STRING, EthereumBlock::getLogsBloom);
        addBlockVariable(BLOCK_MINER, "address", EthereumBlock::getMiner);
        addBlockVariable(BLOCK_NONCE, "int", EthereumBlock::getNonce);
        addBlockVariable(BLOCK_NUMBER, "int", EthereumBlock::getNumber);
        addBlockVariable(BLOCK_PARENT_HASH, TYPE_BYTES, EthereumBlock::getParentHash);
        addBlockVariable(BLOCK_RECEIPTS_ROOT, TYPE_BYTES, EthereumBlock::getReceiptsRoot);
        addBlockVariable(BLOCK_SHA3_UNCLES, TYPE_BYTES, EthereumBlock::getSha3uncles);
        addBlockVariable(BLOCK_SIZE, "int", EthereumBlock::getSize);
        addBlockVariable(BLOCK_STATE_ROOT, TYPE_BYTES, EthereumBlock::getStateRoot);
        addBlockVariable(BLOCK_TIMESTAMP, "int", EthereumBlock::getTimestamp);
        addBlockVariable(BLOCK_TOTAL_DIFFICULTY, "int", EthereumBlock::getTotalDifficulty);
        addBlockVariable(BLOCK_TRANSACTIONS, "int", block -> BigInteger.valueOf(block.transactionCount()));
        addBlockVariable(BLOCK_TRANSACTION_ROOT, TYPE_STRING, EthereumBlock::getTransactionsRoot);
    }

    private static void addBlockVariable(String name, String type, Function<EthereumBlock, Object> blockValueExtractor) {
        EthereumVariable.addVariable(BLOCK_VARIABLES, name, type, state -> blockValueExtractor.apply(state.getReader().getCurrentBlock()));
    }
}
