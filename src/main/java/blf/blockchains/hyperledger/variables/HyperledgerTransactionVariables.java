package blf.blockchains.hyperledger.variables;

import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.values.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * TransactionVariables
 */
public class HyperledgerTransactionVariables {
    static final Set<Variable> TRANSACTION_VARIABLES;

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
    public static final String TX_CUMULATIVE_GAS_USED = "tx.cumulativeGasUsed";
    public static final String TX_GAS_USED = "tx.gasUsed";
    public static final String TX_CONTRACT_ADRESS = "tx.contractAddress";
    public static final String TX_LOGS_BLOOM = "tx.logsBloom";
    public static final String TX_ROOT = "tx.root";
    public static final String TX_STATUS = "tx.status";
    public static final String TX_SUCCESS = "tx.success";

    private static final String TYPE_STRING = "string";

    static {
        TRANSACTION_VARIABLES = new HashSet<>();
        addTransactionVariable(TX_FROM, "address", EthereumTransaction::getFrom);
        addTransactionVariable(TX_GASPRICE, "int", EthereumTransaction::getGasPrice);
        addTransactionVariable(TX_GAS, "int", EthereumTransaction::getGas);
        addTransactionVariable(TX_HASH, "bytes", EthereumTransaction::getHash);
        addTransactionVariable(TX_TO, "address", EthereumTransaction::getTo);
        addTransactionVariable(TX_BLOCKNUMBER, "int", EthereumTransaction::getBlockNumber);
        addTransactionVariable(TX_V, "int", EthereumTransaction::getV);
        addTransactionVariable(TX_R, TYPE_STRING, EthereumTransaction::getR);
        addTransactionVariable(TX_VALUE, "int", EthereumTransaction::getValue);
        addTransactionVariable(TX_BLOCKHASH, "bytes", EthereumTransaction::getBlockHash);
        addTransactionVariable(TX_INPUT, TYPE_STRING, EthereumTransaction::getInput);
        addTransactionVariable(TX_TRANSACTIONINDEX, "int", EthereumTransaction::getTransactionIndex);
        addTransactionVariable(TX_NONCE, "int", EthereumTransaction::getNonce);
        addTransactionVariable(TX_S, TYPE_STRING, EthereumTransaction::getS);
        addTransactionVariable(TX_CUMULATIVE_GAS_USED, "int", EthereumTransaction::getCumulativeGasUsed);
        addTransactionVariable(TX_GAS_USED, "int", EthereumTransaction::getGasUsed);
        addTransactionVariable(TX_CONTRACT_ADRESS, TYPE_STRING, EthereumTransaction::getContractAddress);
        addTransactionVariable(TX_LOGS_BLOOM, TYPE_STRING, EthereumTransaction::getLogsBloom);
        addTransactionVariable(TX_ROOT, TYPE_STRING, EthereumTransaction::getRoot);
        addTransactionVariable(TX_STATUS, TYPE_STRING, EthereumTransaction::getStatus);
        addTransactionVariable(TX_SUCCESS, "bool", EthereumTransaction::isSuccessful);
    }

    private static void addTransactionVariable(String name, String type, ValueExtractor<EthereumTransaction> transactionValueExtractor) {
        Variable.addVariable(TRANSACTION_VARIABLES, name, type, state -> {
            final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
            final EthereumDataReader ethereumReader = ethereumProgramState.getReader();

            final EthereumTransaction tx = ethereumReader.getCurrentTransaction() == null
                ? ethereumReader.getCurrentLogEntry().getTransaction()
                : ethereumReader.getCurrentTransaction();

            return transactionValueExtractor.extractValue(tx);
        });
    }

    @FunctionalInterface
    private interface ValueExtractor<T> {
        Object extractValue(T entity) throws ProgramException;
    }
}
