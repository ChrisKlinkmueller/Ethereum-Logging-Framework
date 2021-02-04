package blf.blockchains.ethereum.reader;

import blf.core.exceptions.ExceptionHandler;
import blf.core.readers.DataReader;

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
            exceptionHandler.handleException("Already connected to Ethereum node.");
            return;
        }

        this.client = Web3jClient.connectWebsocket(url);
    }

    @Override
    public void connectIpc(String path) {
        if (this.client != null) {
            this.exceptionHandler.handleException("Already connected to Ethereum node.", new NullPointerException());

            return;
        }

        this.client = Web3jClient.connectIpc(path);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

}
