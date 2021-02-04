package blf.blockchains.ethereum.reader;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * EthereumTransaction
 */
public abstract class EthereumTransaction {
    private EthereumBlock block;
    private final List<EthereumLogEntry> logs;

    protected EthereumTransaction() {
        this.logs = new ArrayList<>();
    }

    public EthereumBlock getBlock() {
        return this.block;
    }

    public void setBlock(EthereumBlock block) {
        this.block = block;
    }

    public String getBlockHash() {
        return this.block.getHash();
    }

    public BigInteger getBlockNumber() {
        return this.block.getNumber();
    }

    public void addLog(EthereumLogEntry log) {
        this.logs.add(log);
    }

    @SuppressWarnings("unused")
    public int logCount() {
        return this.logs.size();
    }

    @SuppressWarnings("unused")
    public EthereumLogEntry getLog(int index) {
        return this.logs.get(index);
    }

    public Stream<EthereumLogEntry> logStream() {
        return this.logs.stream();
    }

    public Boolean isSuccessful() {
        final String status = this.getStatus();

        if (status == null) {
            return false;
        }

        return status.equals("0x1");
    }

    public abstract String getFrom();

    public abstract BigInteger getGas();

    public abstract BigInteger getGasPrice();

    public abstract String getHash();

    public abstract String getInput();

    public abstract BigInteger getNonce();

    public abstract String getR();

    public abstract String getS();

    public abstract String getTo();

    public abstract BigInteger getTransactionIndex();

    public abstract BigInteger getV();

    public abstract BigInteger getValue();

    public abstract BigInteger getCumulativeGasUsed();

    public abstract BigInteger getGasUsed();

    public abstract String getContractAddress();

    public abstract String getLogsBloom();

    public abstract String getRoot();

    public abstract String getStatus();
}
