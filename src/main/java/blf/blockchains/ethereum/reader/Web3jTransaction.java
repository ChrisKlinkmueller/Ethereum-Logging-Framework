package blf.blockchains.ethereum.reader;

import blf.core.exceptions.ExceptionHandler;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.function.Function;

/**
 * Web3jTransaction
 */
class Web3jTransaction extends EthereumTransaction {
    private final Transaction tx;
    private final Web3jClient client;

    private final ExceptionHandler exceptionHandler;

    private TransactionReceipt receipt;

    public Web3jTransaction(final Web3jClient client, final EthereumBlock block, final Transaction tx) {
        this.tx = tx;
        this.setBlock(block);
        this.client = client;

        this.exceptionHandler = new ExceptionHandler();
    }

    @Override
    public String getFrom() {
        return this.tx.getFrom();
    }

    @Override
    public BigInteger getGas() {
        return this.tx.getGas();
    }

    @Override
    public BigInteger getGasPrice() {
        return this.tx.getGasPrice();
    }

    @Override
    public String getHash() {
        return this.tx.getHash();
    }

    @Override
    public String getInput() {
        return this.tx.getInput();
    }

    @Override
    public BigInteger getNonce() {
        return this.tx.getNonce();
    }

    @Override
    public String getR() {
        return this.tx.getR();
    }

    @Override
    public String getS() {
        return this.tx.getS();
    }

    @Override
    public String getTo() {
        return this.tx.getTo();
    }

    @Override
    public BigInteger getTransactionIndex() {
        return this.tx.getTransactionIndex();
    }

    @Override
    public BigInteger getV() {
        return BigInteger.valueOf(this.tx.getV());
    }

    @Override
    public BigInteger getValue() {
        return this.tx.getValue();
    }

    @Override
    public BigInteger getCumulativeGasUsed() {
        return this.loadReceipt(TransactionReceipt::getCumulativeGasUsed);
    }

    @Override
    public BigInteger getGasUsed() {
        return this.loadReceipt(TransactionReceipt::getGasUsed);
    }

    @Override
    public String getContractAddress() {
        return this.loadReceipt(TransactionReceipt::getContractAddress);
    }

    @Override
    public String getLogsBloom() {
        return this.loadReceipt(TransactionReceipt::getLogsBloom);
    }

    @Override
    public String getRoot() {
        return this.loadReceipt(TransactionReceipt::getRoot);
    }

    @Override
    public String getStatus() {
        return this.loadReceipt(TransactionReceipt::getStatus);
    }

    private <T> T loadReceipt(Function<TransactionReceipt, T> attributeAccessor) {
        if (this.receipt == null) {
            this.receipt = this.client.queryTransactionReceipt(this.getHash());
        }

        if (this.receipt == null) {
            this.exceptionHandler.handleException("The query transaction receipt is null.", new NullPointerException());

            return null;
        }

        return attributeAccessor.apply(this.receipt);
    }

}
