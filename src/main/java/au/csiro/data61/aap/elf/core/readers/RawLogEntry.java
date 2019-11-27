package au.csiro.data61.aap.elf.core.readers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * EthereumLog
 */
public class RawLogEntry extends EthereumLogEntry {
    private boolean removed;
    private BigInteger logIndex;
    private String address;
    private String data;
    private List<String> topics;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public BigInteger getLogIndex() {
        return this.logIndex;
    }

    public void setLogIndex(BigInteger logIndex) {
        this.logIndex = logIndex;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void setTopics(List<String> topics) {
        this.topics = new ArrayList<>(topics);
    }

    public List<String> getTopics() {
        return topics;
    }
}