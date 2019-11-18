package au.csiro.data61.aap.etl;

import java.util.stream.Stream;

import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.rpc.EthereumClient;
import au.csiro.data61.aap.rpc.EthereumLogEntry;
import au.csiro.data61.aap.rpc.EthereumTransaction;

/**
 * EthereumSources
 */
public class EthereumSources {
    private EthereumClient client;
    private EthereumBlock currentBlock;
    private EthereumTransaction currentTransaction;

    public EthereumClient getClient() {
        return client;
    }

    public void setClient(EthereumClient client) {
        this.client = client;
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
        return this.currentBlock == null 
            ? Stream.empty() 
            : this.currentBlock.transactionStream();
    }

    public Stream<EthereumLogEntry> logEntryStream() {
        return this.currentTransaction == null
            ? (
                this.currentBlock == null 
                    ? Stream.empty() 
                    : this.currentBlock.transactionStream().flatMap(EthereumTransaction::logStream)
            )
            : this.currentTransaction.logStream();
    }
    
}