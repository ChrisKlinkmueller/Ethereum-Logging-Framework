package au.csiro.data61.aap.etl.core.readers;

import java.math.BigInteger;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * EthereumTransaction
 */
public class RawTransaction extends EthereumTransaction {
    private String from;
    private BigInteger gas;
    private BigInteger gasPrice;
    private String hash;
    private String input;
    private BigInteger nonce;
    private String to;
    private BigInteger transactionIndex;
    private BigInteger value;
    private BigInteger v;
    private String r;
    private String s;
    private BigInteger cumulativeGasUsed;
    private BigInteger gasUsed;
    private String contractAddress;
    private String logsBloom;
    private String root;
    private String status;

    @Override
    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public BigInteger getGas() {
        return this.gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    @Override
    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    @Override
    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public BigInteger getNonce() {
        return this.nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    @Override
    public String getR() {
        return this.r;
    }

    public void setR(String r) {
        this.r = r;
    }

    @Override
    public String getS() {
        return this.s;
    }

    public void setS(String s) {
        this.s = s;
    }

    @Override
    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public BigInteger getTransactionIndex() {
        return this.transactionIndex;
    }

    public void setTransactionIndex(BigInteger transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    @Override
    public BigInteger getV() {
        return this.v;
    }

    public void setV(BigInteger v) {
        this.v = v;
    }

    @Override
    public BigInteger getValue() {
        return this.value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public BigInteger getCumulativeGasUsed() throws ProgramException {
        return this.cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(BigInteger cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    @Override
    public BigInteger getGasUsed() throws ProgramException {
        return this.gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    @Override
    public String getContractAddress() throws ProgramException {
        return this.contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public String getLogsBloom() throws ProgramException {
        return this.logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    @Override
    public String getRoot() throws ProgramException {
        return this.root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @Override
    public String getStatus() throws ProgramException {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}