package au.csiro.data61.aap.elf.core.readers;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.web3j.protocol.core.methods.response.Log;

/**
 * Web3jLogEntry
 */
class Web3jLogEntry extends EthereumLogEntry {
    private final Log log;

    protected Web3jLogEntry(EthereumTransaction tx, Log log) {
        assert tx != null;
        assert log != null;
        this.setTransaction(tx);
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
    public List<String> getTopics() {
        return Collections.unmodifiableList(this.log.getTopics());
    }


}
