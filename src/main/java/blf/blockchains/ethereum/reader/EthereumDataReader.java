package blf.blockchains.ethereum.reader;

import blf.core.exceptions.ExceptionHandler;
import blf.core.readers.DataReader;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

/**
 * EthereumSources
 */
public class EthereumDataReader extends DataReader<EthereumClient, EthereumBlock, EthereumTransaction, EthereumLogEntry> {

    private final ExceptionHandler exceptionHandler;

    public EthereumDataReader() {
        exceptionHandler = new ExceptionHandler();
    }

    public Stream<EthereumTransaction> transactionStream() {
        return this.currentBlock == null ? Stream.empty() : this.currentBlock.transactionStream();
    }

    public Stream<EthereumLogEntry> logEntryStream() {
        if (this.currentTransaction == null) {
            return this.currentBlock == null
                ? Stream.empty()
                : this.currentBlock.transactionStream().flatMap(EthereumTransaction::logStream);
        } else {
            return this.currentTransaction.logStream();
        }
    }

    public void connect(String url) {

        if (this.client != null) {
            exceptionHandler.handleExceptionAndDecideOnAbort("Already connected to Ethereum node.");
            return;
        }

        try {
            this.client = Web3jClient.connectWebsocket(url);
        } catch (ConnectException | URISyntaxException e) {
            final String exceptionMsg = String.format("Error when connecting to Ethereum node via websocket using URL '%s'.", url);
            this.exceptionHandler.handleExceptionAndDecideOnAbort(exceptionMsg, e);
        }
    }

    public void connectIpc(String path) {
        if (this.client != null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Already connected to Ethereum node.", new NullPointerException());
        }

        try {
            this.client = Web3jClient.connectIpc(path);
        } catch (ConnectException e) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Error when connecting to Ethereum node via ipc.", e);
        }
    }

    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

}
