package au.csiro.data61.aap.etl.core.readers;

import java.math.BigInteger;

/**
 * EthereumTransaction
 */
public class RawTransaction extends EthereumTransaction {
    private String from;
    private BigInteger gas;
    private BigInteger gasPrice;
    private String hash;
    private String input;
    private BigInteger nonce;
    private String to;
    private BigInteger transactionIndex;
    private BigInteger value;
    private BigInteger v;
    private String r;
    private String s;
    
    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public BigInteger getGas() {
        return this.gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public BigInteger getNonce() {
        return this.nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public String getR() {
        return this.r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getS() {
        return this.s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getTransactionIndex() {
        return this.transactionIndex;
    }

    public void setTransactionIndex(BigInteger transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public BigInteger getV() {
        return this.v;
    }

    public void setV(BigInteger v) {
        this.v = v;
    }

    public BigInteger getValue() {
        return this.value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }
}