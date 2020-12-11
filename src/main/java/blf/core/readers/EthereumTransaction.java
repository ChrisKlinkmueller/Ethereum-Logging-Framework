package blf.core.readers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import blf.core.exceptions.ProgramException;

/**
 * EthereumTransaction
 */
public abstract class EthereumTransaction {
    private EthereumBlock block;
    private final List<EthereumLogEntry> logs;

    public EthereumTransaction() {
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
        assert log != null;
        this.logs.add(log);
    }

    public int logCount() {
        return this.logs.size();
    }

    public EthereumLogEntry getLog(int index) {
        assert 0 <= index && index < this.logs.size();
        return this.logs.get(index);
    }

    public Stream<EthereumLogEntry> logStream() {
        return this.logs.stream();
    }

    public Boolean isSuccessful() throws ProgramException {
        final String status = this.getStatus();
        if (status == null) {
            return false;
        } else {
            return status.equals("0x1");
        }
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

    public abstract BigInteger getCumulativeGasUsed() throws ProgramException;

    public abstract BigInteger getGasUsed() throws ProgramException;

    public abstract String getContractAddress() throws ProgramException;

    public abstract String getLogsBloom() throws ProgramException;

    public abstract String getRoot() throws ProgramException;

    public abstract String getStatus() throws ProgramException;
}
