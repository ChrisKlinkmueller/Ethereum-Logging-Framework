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
import au.csiro.data61.aap.etl.core.readers.EthereumBlock;
import au.csiro.data61.aap.etl.core.readers.EthereumTransaction;

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

	public static final String TX_BLOCKNUMBER = "tx.blockNumber";
	public static final String TX_BLOCKHASH = "tx.blockHash";
	public static final String TX_FROM = "tx.from";
	public static final String TX_TRANSACTIONINDEX = "tx.transactionIndex";
	public static final String TX_TO = "tx.to";
	public static final String TX_INPUT = "tx.input";
	public static final String TX_S = "tx.s";
	public static final String TX_HASH = "tx.hash";
	public static final String TX_R = "tx.r";
	public static final String TX_GAS = "tx.gas";
	public static final String TX_GASPRICE = "tx.gasPrice";
	public static final String TX_V = "tx.v";
	public static final String TX_NONCE = "tx.nonce";
	public static final String TX_VALUE = "tx.value";

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
        addTransactionVariable(TX_FROM, "address", EthereumTransaction::getFrom);
        addTransactionVariable(TX_GASPRICE, "int", EthereumTransaction::getGasPrice);
        addTransactionVariable(TX_GAS, "int", EthereumTransaction::getGas);
        addTransactionVariable(TX_HASH, "bytes", EthereumTransaction::getHash);
        addTransactionVariable(TX_TO, "address", EthereumTransaction::getTo);
        addTransactionVariable(TX_BLOCKNUMBER, "int", EthereumTransaction::getBlockNumber);
        addTransactionVariable(TX_V, "int", EthereumTransaction::getV);
        addTransactionVariable(TX_R, "string", EthereumTransaction::getR);
        addTransactionVariable(TX_VALUE, "int", EthereumTransaction::getValue);
        addTransactionVariable(TX_BLOCKHASH, "bytes", EthereumTransaction::getBlockHash);
        addTransactionVariable(TX_INPUT, "string", EthereumTransaction::getInput);
        addTransactionVariable(TX_TRANSACTIONINDEX, "int", EthereumTransaction::getTransactionIndex);
        addTransactionVariable(TX_NONCE, "int", EthereumTransaction::getNonce);
        addTransactionVariable(TX_S, "string", EthereumTransaction::getS);
    
        LOG_ENTRY_VARIABLES = new HashSet<>();
    }

    private static void addBlockVariable(String name, String type, Function<EthereumBlock, Object> blockValueExtractor) {
        addVariable(BLOCK_VARIABLES, name, type, state -> blockValueExtractor.apply(state.getDataSource().getCurrentBlock()));
    }

    private static void addTransactionVariable(String name, String type, Function<EthereumTransaction, Object> blockValueExtractor) {
        addVariable(BLOCK_VARIABLES, name, type, state -> blockValueExtractor.apply(state.getDataSource().getCurrentTransaction()));
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