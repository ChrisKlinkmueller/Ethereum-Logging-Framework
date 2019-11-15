package au.csiro.data61.aap.rpc;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.web3j.protocol.core.methods.response.Log;

/**
 * Web3jLogEntry
 */
class Web3jLogEntry extends EthereumLogEntry {
    private final Log log;

    protected Web3jLogEntry(EthereumTransaction tx, Log log) {
        super(tx);
        assert log != null;
        this.log = log;
    }

    @Override
    public String getAddress() {
        return this.log.getAddress();
    }

    @Override
    public String getData() {
        return this.log.getData();
    }

    @Override
    public BigInteger getLogIndex() {
        return this.log.getLogIndex();
    }

    @Override
    public boolean isRemoved() {
        return this.log.isRemoved();
    }

    @Override
    public int topicCount() {
        return this.log.getTopics().size();
    }

    @Override
    public String getTopic(int index) {
        return this.log.getTopics().get(index);
    }

    @Override
    public Stream<String> topicStream() {
        return this.log.getTopics().stream();
    }

    
}