package au.csiro.data61.aap.rpc;

import java.math.BigInteger;
import java.util.List;

/**
 * Block
 */
public class RawBlock extends EthereumBlock {    
    private final BigInteger number;
    private final String hash;
    private final String parentHash;
    private final BigInteger nonce;
    private final String sha3uncles;
    private final String logsBloom;
    private final String transactionsRoot;
    private final String stateRoot;
    private final String receiptsRoot;
    private final String miner;
    private final BigInteger difficulty;
    private final BigInteger totalDifficulty;
    private final String extraData;
    private final BigInteger size;
    private final BigInteger gasLimit;
    private final BigInteger gasUsed;
    private final BigInteger timestamp;
    private final List<String> uncles;
    
    public RawBlock (
        BigInteger number,
        String hash,
        String parentHash,
        BigInteger nonce,
        String sha3uncles,
        String logsBloom,
        String transactionsRoot,
        String stateRoot,
        String receiptsRoot,
        String miner,
        BigInteger difficulty,
        BigInteger totalDifficulty,
        String extraData,
        BigInteger size,
        BigInteger gasLimit,
        BigInteger gasUsed,
        BigInteger timestamp,
        List<String> uncles
    ) {
        this.number = number;
        this.hash = hash;
        this.parentHash = parentHash;
        this.nonce = nonce;
        this.sha3uncles = sha3uncles;
        this.logsBloom = logsBloom;
        this.transactionsRoot = transactionsRoot;
        this.stateRoot = stateRoot;
        this.receiptsRoot = receiptsRoot;
        this.miner = miner;
        this.difficulty = difficulty;
        this.totalDifficulty = totalDifficulty;
        this.extraData = extraData;
        this.size = size;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.timestamp = timestamp;
        this.uncles = uncles;
    }

    public String getHash() {
        return this.hash;
    }

    public BigInteger getDifficulty() {
        return this.difficulty;
    }

    public String getExtraData() {
        return this.extraData;
    }

    public BigInteger getGasLimit() {
        return this.gasLimit;
    }

    public BigInteger getGasUsed() {
        return this.gasUsed;
    }

    public String getLogsBloom() {
        return this.logsBloom;
    }

    public String getMiner() {
        return this.miner;
    }

    public BigInteger getNonce() {
        return this.nonce;
    }

    public BigInteger getNumber() {
        return this.number;
    }

    public String getParentHash() {
        return this.parentHash;
    }

    public String getReceiptsRoot() {
        return this.receiptsRoot;
    }

    public String getSha3uncles() {
        return this.sha3uncles;
    }

    public BigInteger getSize() {
        return this.size;
    }

    public String getStateRoot() {
        return this.stateRoot;
    }

    public BigInteger getTimestamp() {
        return this.timestamp;
    }

    public BigInteger getTotalDifficulty() {
        return this.totalDifficulty;
    }

    public String getTransactionsRoot() {
        return this.transactionsRoot;
    }

    public List<String> getUncles() {
        return this.uncles;
    }
}