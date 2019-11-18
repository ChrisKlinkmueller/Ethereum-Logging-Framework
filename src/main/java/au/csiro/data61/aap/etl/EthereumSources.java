package au.csiro.data61.aap.etl;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.rpc.EthereumClient;
import au.csiro.data61.aap.rpc.EthereumLogEntry;
import au.csiro.data61.aap.rpc.EthereumTransaction;
import au.csiro.data61.aap.rpc.Web3jClient;

/**
 * EthereumSources
 */
public class EthereumSources {
    private EthereumClient client;
    private EthereumBlock currentBlock;
    private EthereumTransaction currentTransaction;

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

    public Stream<EthereumTransaction> transactionStream() {
        return this.currentBlock == null ? Stream.empty() : this.currentBlock.transactionStream();
    }

    public Stream<EthereumLogEntry> logEntryStream() {
        return this.currentTransaction == null
                ? (this.currentBlock == null ? Stream.empty()
                        : this.currentBlock.transactionStream().flatMap(EthereumTransaction::logStream))
                : this.currentTransaction.logStream();
    }

    public void connect(String url) throws EtlException {
        assert url != null;
        if (this.client != null) {
            throw new EtlException("Already connected to Ethereum node.");
        }

        try {
            this.client = new Web3jClient(url);
        } catch (ConnectException | URISyntaxException e) {
            throw new EtlException(String.format("Error when connecting to Ethereum node using URL '%s'.", url), e);
        }
    }
    
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }
    
}