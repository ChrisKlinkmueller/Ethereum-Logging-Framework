package au.csiro.data61.aap.rpc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * EthereumLog
 */
public class RawLogEntry extends EthereumLogEntry {
    private final boolean removed;
    private final BigInteger logIndex;
    private final String address;
    private final String data;
    private final List<String> topics;
    
    RawLogEntry(
        EthereumTransaction tx,
        boolean removed,
        BigInteger logIndex,
        String address,
        String data,
        List<String> topics
    )
    {
        super(tx);
        this.removed = removed;
        this.logIndex = logIndex;
        this.address = address;
        this.data = data;
        this.topics = topics == null ? new ArrayList<>() : new ArrayList<>(topics);
    }

    public String getAddress() {
        return address;
    }

    public String getData() {
        return data;
    }

    public BigInteger getLogIndex() {
        return this.logIndex;
    }

    public boolean isRemoved() {
        return removed;
    }

    public int topicCount() {
        return this.topics.size();
    }

    public String getTopic(int index) {
        assert 0 <= index && index < this.topicCount();
        return this.topics.get(index);
    }

    public Stream<String> topicStream() {
        return this.topics.stream();
    }
}