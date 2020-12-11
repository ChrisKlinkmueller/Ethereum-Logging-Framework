package blf.core.readers;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import blf.core.exceptions.ProgramException;

/**
 * EthereumSources
 */
public class DataReader {
    private EthereumClient client;
    private EthereumBlock currentBlock;
    private EthereumTransaction currentTransaction;
    private EthereumLogEntry currentLogEntry;

    public EthereumClient getClient() {
        return this.client;
    }

    public EthereumBlock getCurrentBlock() {
        return this.currentBlock;
    }

    public void setCurrentBlock(EthereumBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    public EthereumTransaction getCurrentTransaction() {
        return this.currentTransaction;
    }

    public void setCurrentTransaction(EthereumTransaction currentTransaction) {
        this.currentTransaction = currentTransaction;
    }

    public EthereumLogEntry getCurrentLogEntry() {
        return this.currentLogEntry;
    }

    public void setCurrentLogEntry(EthereumLogEntry currentLogEntry) {
        this.currentLogEntry = currentLogEntry;
    }

    public Stream<EthereumTransaction> transactionStream() {
        return this.currentBlock == null ? Stream.empty() : this.currentBlock.transactionStream();
    }

    public Stream<EthereumLogEntry> logEntryStream() {
        return this.currentTransaction == null
            ? (this.currentBlock == null ? Stream.empty() : this.currentBlock.transactionStream().flatMap(EthereumTransaction::logStream))
            : this.currentTransaction.logStream();
    }

    public void connect(String url) throws ProgramException {
        assert url != null;
        if (this.client != null) {
            throw new ProgramException("Already connected to Ethereum node.");
        }

        try {
            this.client = Web3jClient.connectWebsocket(url);
        } catch (ConnectException | URISyntaxException e) {
            throw new ProgramException(String.format("Error when connecting to Ethereum node via websocket using URL '%s'.", url), e);
        }
    }

    public void connectIpc(String path) throws ProgramException {
        assert path != null;
        if (this.client != null) {
            throw new ProgramException("Already connected to Ethereum node.");
        }

        try {
            this.client = Web3jClient.connectIpc(path);
        } catch (ConnectException e) {
            throw new ProgramException(String.format("Error when connecting to Ethereum node via ipc.", path), e);
        }
    }

    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

}
