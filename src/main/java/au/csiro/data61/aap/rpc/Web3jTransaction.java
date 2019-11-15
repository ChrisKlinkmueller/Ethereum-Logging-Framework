package au.csiro.data61.aap.rpc;

import java.math.BigInteger;

import org.web3j.protocol.core.methods.response.Transaction;

/**
 * Web3jTransaction
 */
class Web3jTransaction extends EthereumTransaction {
    private final Transaction tx;

    public Web3jTransaction(EthereumBlock block, Transaction tx) {
        super(block);
        assert tx != null;
        this.tx = tx;
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

    
}