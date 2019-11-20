package au.csiro.data61.aap.etl.core.readers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block
 */
public class RawBlock extends EthereumBlock {    
    private BigInteger number;
    private String hash;
    private String parentHash;
    private BigInteger nonce;
    private String sha3uncles;
    private String logsBloom;
    private String transactionsRoot;
    private String stateRoot;
    private String receiptsRoot;
    private String miner;
    private BigInteger difficulty;
    private BigInteger totalDifficulty;
    private String extraData;
    private BigInteger size;
    private BigInteger gasLimit;
    private BigInteger gasUsed;
    private BigInteger timestamp;
    private List<String> uncles;
    
    public String getHash() {
        return this.hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }

    public BigInteger getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(BigInteger difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getExtraData() {
        return this.extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public BigInteger getGasLimit() {
        return this.gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasUsed() {
        return this.gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getLogsBloom() {
        return this.logsBloom;
    }
    
    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public String getMiner() {
        return this.miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public BigInteger getNonce() {
        return this.nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public BigInteger getNumber() {
        return this.number;
    }

    public void setNumber(BigInteger number) {
        this.number = number;
    }

    public String getParentHash() {
        return this.parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getReceiptsRoot() {
        return this.receiptsRoot;
    }

    public void setReceiptsRoot(String receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public String getSha3uncles() {
        return this.sha3uncles;
    }

    public void setSha3uncles(String sha3uncles) {
        this.sha3uncles = sha3uncles;
    }

    public BigInteger getSize() {
        return this.size;
    }

    public void setSize(BigInteger size) {
        this.size = size;
    }

    public String getStateRoot() {
        return this.stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public BigInteger getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public BigInteger getTotalDifficulty() {
        return this.totalDifficulty;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public String getTransactionsRoot() {
        return this.transactionsRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    public List<String> getUncles() {
        return Collections.unmodifiableList(this.uncles);
    }

    public void setUncles(List<String> uncles) {
        this.uncles = new ArrayList<>(uncles);
    }
}