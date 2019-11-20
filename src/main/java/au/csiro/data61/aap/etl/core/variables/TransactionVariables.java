package au.csiro.data61.aap.etl.core.variables;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import au.csiro.data61.aap.etl.core.readers.EthereumTransaction;

/**
 * TransactionVariables
 */
public class TransactionVariables {
    static final Set<EthereumVariable> TRANSACTION_VARIABLES;

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
    }

    private static void addTransactionVariable(String name, String type, Function<EthereumTransaction, Object> blockValueExtractor) {
        EthereumVariable.addVariable(TRANSACTION_VARIABLES, name, type, state -> blockValueExtractor.apply(state.getReader().getCurrentTransaction()));
    }
}