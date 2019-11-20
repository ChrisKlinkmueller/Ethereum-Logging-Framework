package au.csiro.data61.aap.etl.library.values;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.web3j.abi.TypeReference;

import au.csiro.data61.aap.etl.core.EtlException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.rpc.EthereumBlock;

/**
 * DataSourceAccessors
 */
public class DataSourceVariables {
    private static final Set<DataSourceVariable> BLOCK_VARIABLES;
    private static final Set<DataSourceVariable> TRANSACTION_VARIABLES;
    private static final Set<DataSourceVariable> LOG_ENTRY_VARIABLES;

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

        TRANSACTION_VARIABLES = new HashSet<>();
        LOG_ENTRY_VARIABLES = new HashSet<>();
    }

    private static void addBlockVariable(String name, String type, Function<EthereumBlock, Object> blockValueExtractor) {
        addVariable(BLOCK_VARIABLES, name, type, state -> blockValueExtractor.apply(state.getDataSource().getCurrentBlock()));
    }

    private static <T> void addVariable(Set<DataSourceVariable> variables, String name, String type, Function<ProgramState, Object> valueExtractor) {
        try {
            variables.add(new DataSourceVariable(name, TypeReference.makeTypeReference(type), valueExtractor));
        }
        catch (Throwable error) {
            error.printStackTrace();
        }
    }

    public static ValueAccessor currentBlockNumberAccessor() {
        return state -> {
            try {
                return state.getDataSource().getClient().queryBlockNumber();
            }
            catch (Throwable error) {
                throw new EtlException("Error when retrieving the current block number.", error);
            }
        };
    }

    public static Instruction createValueCreationInstruction(String attribute) {
        return findVariable(attribute, DataSourceVariable::getValueCreator);
    }

    public static Instruction createValueRemovalInstruction(String attribute) {
        return findVariable(attribute, DataSourceVariable::getValueRemover);
    }

    public static TypeReference<?> getType(String attribute) {
        return findVariable(attribute, DataSourceVariable::getType);
    }

    private static <T> T findVariable(String name, Function<DataSourceVariable, T> mapper) {
        return variableStream()
            .filter(variable -> variable.hasName(name))
            .map(variable -> mapper.apply(variable))
            .findFirst().orElse(null);
    }

    private static Stream<DataSourceVariable> variableStream() {
        return Stream.concat(BLOCK_VARIABLES.stream(), Stream.concat(TRANSACTION_VARIABLES.stream(), LOG_ENTRY_VARIABLES.stream()));
    }
    
}