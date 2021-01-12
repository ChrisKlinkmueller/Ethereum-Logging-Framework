package blf.blockchains.ethereum.reader;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import blf.core.exceptions.ProgramException;
import blf.core.readers.DataReader;
import io.reactivex.annotations.NonNull;

/**
 * EthereumSources
 */
public class EthereumDataReader extends DataReader<EthereumClient, EthereumBlock, EthereumTransaction, EthereumLogEntry> {

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

    public void connect(String url) throws ProgramException {
        if (this.client != null) {
            throw new ProgramException("Already connected to Ethereum node.");
        }

        try {
            this.client = Web3jClient.connectWebsocket(url);
        } catch (ConnectException | URISyntaxException e) {
            throw new ProgramException(String.format("Error when connecting to Ethereum node via websocket using URL '%s'.", url), e);
        }
    }

    public void connectIpc(@NonNull String path) throws ProgramException {
        if (this.client != null) {
            throw new ProgramException("Already connected to Ethereum node.");
        }

        try {
            this.client = Web3jClient.connectIpc(path);
        } catch (ConnectException e) {
            throw new ProgramException("Error when connecting to Ethereum node via ipc.", e);
        }
    }

    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

}
