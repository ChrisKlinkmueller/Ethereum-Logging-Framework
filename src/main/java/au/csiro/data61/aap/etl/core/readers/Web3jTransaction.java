package au.csiro.data61.aap.etl.core.readers;

import java.math.BigInteger;
import java.util.function.Function;

import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * Web3jTransaction
 */
class Web3jTransaction extends EthereumTransaction {
    private final Transaction tx;
    private TransactionReceipt receipt;
    private final Web3jClient client;

    public Web3jTransaction(final Web3jClient client, final EthereumBlock block, final Transaction tx) {
        assert block != null;
        assert tx != null;
        assert client != null;
        this.tx = tx;
        this.setBlock(block);
        this.client = client;
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
    public BigInteger getCumulativeGasUsed() throws ProgramException {
        return this.loadReceipt(TransactionReceipt::getCumulativeGasUsed);
    }

    @Override
    public BigInteger getGasUsed() throws ProgramException {
        return this.loadReceipt(TransactionReceipt::getGasUsed);
    }

    @Override
    public String getContractAddress() throws ProgramException {
        return this.loadReceipt(TransactionReceipt::getContractAddress);
    }

    @Override
    public String getLogsBloom() throws ProgramException {
        return this.loadReceipt(TransactionReceipt::getLogsBloom);
    }

    @Override
    public String getRoot() throws ProgramException {
        return this.loadReceipt(TransactionReceipt::getRoot);
    }

    @Override
    public String getStatus() throws ProgramException {
        return this.loadReceipt(TransactionReceipt::getStatus);
    }

    private <T> T loadReceipt(Function<TransactionReceipt, T> attributeAccessor) throws ProgramException {
        if (this.receipt == null) {
            try {
                this.receipt = this.client.queryTransactionReceipt(this.getHash());
            }
            catch (Throwable cause) {
                throw new ProgramException(String.format("Error loading attributes for transaction '%s'.", this.getHash()), cause);
            }
        }
        return attributeAccessor.apply(this.receipt);
    }

    
}