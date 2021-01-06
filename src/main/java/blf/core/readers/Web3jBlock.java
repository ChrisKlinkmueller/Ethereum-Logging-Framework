package blf.core.readers;

import java.math.BigInteger;
import java.util.List;

import io.reactivex.annotations.NonNull;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

/**
 * Web3jBlock
 */
class Web3jBlock extends EthereumBlock {
    private final Block block;

    public Web3jBlock(@NonNull Block block) {
        this.block = block;
    }

    @Override
    public String getHash() {
        return this.block.getHash();
    }

    @Override
    public BigInteger getDifficulty() {
        return this.block.getDifficulty();
    }

    @Override
    public String getExtraData() {
        return this.block.getExtraData();
    }

    @Override
    public BigInteger getGasLimit() {
        return this.block.getGasLimit();
    }

    @Override
    public BigInteger getGasUsed() {
        return this.block.getGasUsed();
    }

    @Override
    public String getLogsBloom() {
        return this.block.getLogsBloom();
    }

    @Override
    public String getMiner() {
        return this.block.getMiner();
    }

    @Override
    public BigInteger getNonce() {
        return this.block.getNonce();
    }

    @Override
    public BigInteger getNumber() {
        return this.block.getNumber();
    }

    @Override
    public String getParentHash() {
        return this.block.getParentHash();
    }

    @Override
    public String getReceiptsRoot() {
        return this.block.getReceiptsRoot();
    }

    @Override
    public String getSha3uncles() {
        return this.block.getSha3Uncles();
    }

    @Override
    public BigInteger getSize() {
        return this.block.getSize();
    }

    @Override
    public String getStateRoot() {
        return this.block.getStateRoot();
    }

    @Override
    public BigInteger getTimestamp() {
        return this.block.getTimestamp();
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return this.block.getTotalDifficulty();
    }

    @Override
    public String getTransactionsRoot() {
        return this.block.getTransactionsRoot();
    }

    @Override
    public List<String> getUncles() {
        return this.block.getUncles();
    }

}
