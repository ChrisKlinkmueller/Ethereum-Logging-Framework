package au.csiro.data61.aap.rpc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * EthereumBlock
 */
public abstract class EthereumBlock {
    private final List<EthereumTransaction> transactions;

    protected EthereumBlock() {
        this.transactions = new ArrayList<>();
    }

    public abstract String getHash();
    public abstract BigInteger getDifficulty();
    public abstract String getExtraData();
    public abstract BigInteger getGasLimit();
    public abstract BigInteger getGasUsed();
    public abstract String getLogsBloom();
    public abstract String getMiner();
    public abstract BigInteger getNonce();
    public abstract BigInteger getNumber();
    public abstract String getParentHash();
    public abstract String getReceiptsRoot();
    public abstract String getSha3uncles();
    public abstract BigInteger getSize();
    public abstract String getStateRoot();
    public abstract BigInteger getTimestamp();
    public abstract BigInteger getTotalDifficulty();
    public abstract String getTransactionsRoot();
    public abstract List<String> getUncles();

    public void addTransaction(EthereumTransaction tx) {
        assert tx != null;
        this.transactions.add(tx);
    }

    public int transactionCount() {
        return this.transactions.size();
    }

    public EthereumTransaction getTransaction(int index) {
        assert 0 <= index && index < this.transactionCount();
        return this.transactions.get(index);
    }

    public Stream<EthereumTransaction> transactionStream() {
        return this.transactions.stream();
    }
}