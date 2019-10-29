package au.csiro.data61.aap.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * EthereumTransaction
 */
public class EthereumTransaction {
    private final List<EthereumLog> logs;

    private final EthereumBlock block;
    private final String from;
    private final String gas;
    private final String gasPrice;
    private final String hash;
    private final String input;
    private final String nonce;
    private final String to;
    private final String transactionIndex;
    private final String value;
    private final long v;
    private final String r;
    private final String s;

    EthereumTransaction(
        EthereumBlock block,
        String from,
        String gas,
        String gasPrice,
        String hash,
        String input,
        String nonce,
        String to,
        String transactionIndex,
        String value,
        long v,
        String r,
        String s
    )
    {
        this.block = block;
        this.from = from;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.hash = hash;
        this.input = input;
        this.nonce = nonce;
        this.to = to;
        this.transactionIndex = transactionIndex;
        this.value = value;
        this.v = v;
        this.r = r;
        this.s = s;

        this.logs = new ArrayList<>();
    }
    
    public String getBlockHash() {
        return this.block.getHash();
    }

    public String getBlockNumber() {
        return this.block.getNumber();
    }

    public EthereumBlock getBlock() {
        return this.block;
    }
    
    public String getFrom() {
        return this.from;
    }

    public String getGas() {
        return this.gas;
    }

    public String getGasPrice() {
        return this.gasPrice;
    }

    public String getHash() {
        return this.hash;
    }

    public String getInput() {
        return this.input;
    }

    public String getNonce() {
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

    public String getTransactionIndex() {
        return this.transactionIndex;
    }

    public long getV() {
        return this.v;
    }

    public String getValue() {
        return this.value;
    }

    void addLog(EthereumLog log) {
        assert log != null;
        this.logs.add(log);
    }

    public int logCount() {
        return this.logs.size();
    }

    public EthereumLog getLog(int index) {
        assert 0 <= index && index < this.logs.size();
        return this.logs.get(index);
    }

    public Stream<EthereumLog> logStream() {
        return this.logs.stream();
    }
}