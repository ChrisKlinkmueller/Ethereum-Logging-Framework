package au.csiro.data61.aap.rpc;

import java.math.BigInteger;

/**
 * EthereumTransaction
 */
public class RawTransaction extends EthereumTransaction {
    private final String from;
    private final BigInteger gas;
    private final BigInteger gasPrice;
    private final String hash;
    private final String input;
    private final BigInteger nonce;
    private final String to;
    private final BigInteger transactionIndex;
    private final BigInteger value;
    private final BigInteger v;
    private final String r;
    private final String s;

    RawTransaction(
        EthereumBlock block,
        String from,
        BigInteger gas,
        BigInteger gasPrice,
        String hash,
        String input,
        BigInteger nonce,
        String to,
        BigInteger transactionIndex,
        BigInteger value,
        long v,
        String r,
        String s
    )
    {
        super(block);
        this.from = from;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.hash = hash;
        this.input = input;
        this.nonce = nonce;
        this.to = to;
        this.transactionIndex = transactionIndex;
        this.value = value;
        this.v = BigInteger.valueOf(v);
        this.r = r;
        this.s = s;
    }
    
    public String getFrom() {
        return this.from;
    }

    public BigInteger getGas() {
        return this.gas;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public String getHash() {
        return this.hash;
    }

    public String getInput() {
        return this.input;
    }

    public BigInteger getNonce() {
        return this.nonce;
    }

    public String getR() {
        return this.r;
    }

    public String getS() {
        return this.s;
    }

    public String getTo() {
        return this.to;
    }

    public BigInteger getTransactionIndex() {
        return this.transactionIndex;
    }

    public BigInteger getV() {
        return this.v;
    }

    public BigInteger getValue() {
        return this.value;
    }
}