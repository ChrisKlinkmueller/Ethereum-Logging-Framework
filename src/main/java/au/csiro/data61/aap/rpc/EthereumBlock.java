package au.csiro.data61.aap.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Block
 */
public class EthereumBlock {
    private final List<EthereumTransaction> transactions;
    
    private final String number;
    private final String hash;
    private final String parentHash;
    private final String nonce;
    private final String sha3uncles;
    private final String logsBloom;
    private final String transactionsRoot;
    private final String stateRoot;
    private final String receiptsRoot;
    private final String miner;
    private final String difficulty;
    private final String totalDifficulty;
    private final String extraData;
    private final String size;
    private final String gasLimit;
    private final String gasUsed;
    private final String timestamp;
    
    EthereumBlock(
        String number,
        String hash,
        String parentHash,
        String nonce,
        String sha3uncles,
        String logsBloom,
        String transactionsRoot,
        String stateRoot,
        String receiptsRoot,
        String miner,
        String difficulty,
        String totalDifficulty,
        String extraData,
        String size,
        String gasLimit,
        String gasUsed,
        String timestamp
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

        this.transactions = new ArrayList<>();
    }

    public String getHash() {
        return this.hash;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    public String getExtraData() {
        return this.extraData;
    }

    public String getGasLimit() {
        return this.gasLimit;
    }

    public String getGasUsed() {
        return this.gasUsed;
    }

    public String getLogsBloom() {
        return this.logsBloom;
    }

    public String getMiner() {
        return this.miner;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getNumber() {
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

    public String getSize() {
        return this.size;
    }

    public String getStateRoot() {
        return this.stateRoot;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getTotalDifficulty() {
        return this.totalDifficulty;
    }

    public String getTransactionsRoot() {
        return this.transactionsRoot;
    }

    void addTransaction(EthereumTransaction tx) {
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