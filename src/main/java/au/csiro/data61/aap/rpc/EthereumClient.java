package au.csiro.data61.aap.rpc;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

/**
 * EthereumClient
 */
public class EthereumClient {
    private static final Logger LOGGER = Logger.getLogger(EthereumClient.class.getName());
    private static final String URL = "ws://localhost:8546/";

    public static void main(String[] args) {
        EthereumClient client = null;
        try {
            client = new EthereumClient();
            final BigInteger number = client.queryBlockNumber();
            System.out.println("Current Blocknumber: " + number);

            final EthereumBlock block = client.queryBlockData(number);
            System.out.println("\tHash: " + block.getHash());
            System.out.println("\tGas used: " + block.getGasUsed());
            System.out.println("\tTransactions: " + block.transactionCount());
            for (int i = 0; i < Math.min(10, block.transactionCount()); i++) {
                final EthereumTransaction transaction = block.getTransaction(i);
                System.out.println(String.format("\t\tTx (%s): %s -> %s", transaction.getTransactionIndex(), transaction.getFrom(), transaction.getTo()));

                for (int j = 0; j < Math.min(3, transaction.logCount()); j++) {
                    final EthereumLog log = transaction.getLog(j);
                    System.out.println(String.format("\t\t\tLog: %s", log.getData()));
                }
                if (3 < transaction.logCount()) {
                    System.out.println("\t\t\t...");
                }
            }
            if (10 < block.transactionCount()) {
                System.out.println("\t\t...");
            }

            client.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            client.close();
        }
    }

    private final WebSocketService wsService;
    private final Web3j web3j;

    EthereumClient() throws URISyntaxException, ConnectException {
        this(URL);
    }

    public EthereumClient(String url) throws URISyntaxException, ConnectException {
        try {
            final WebSocketClient wsClient = new WebSocketClient(new URI(URL));
            final WebSocketService wsService = new WebSocketService(wsClient, false);
            wsService.connect();

            this.web3j = Web3j.build(wsService);
            this.wsService = wsService;
        }
        catch (URISyntaxException | ConnectException ex) {
            final String message = String.format("Error when connecting to the ethereum client with url '%s'.", url);
            LOGGER.log(Level.SEVERE, message, ex);
            throw ex;
        }
    }

    public void close() {
        this.wsService.close();
    }

    public BigInteger queryBlockNumber() throws IOException {
        try {
            final EthBlockNumber queryResult = this.web3j.ethBlockNumber().send();
            if (queryResult.hasError()) {
                throw new IOException(queryResult.getError().getMessage());
            } else {
                return queryResult.getBlockNumber();
            }
        } catch (IOException ex) {
            final String message = "Error when retrieving the current block number.";
            LOGGER.log(Level.SEVERE, message, ex);
            throw ex;
        }
    }

    public EthereumBlock queryBlockData(BigInteger blockNumber) throws IOException {
        final DefaultBlockParameterNumber number = new DefaultBlockParameterNumber(blockNumber);
        try {
            final EthBlock blockResult = this.web3j.ethGetBlockByNumber(number, true).send();
            if (blockResult.hasError()) {
                throw new IOException(blockResult.getError().getMessage());
            }

            final EthFilter filter = new EthFilter(number, number, new ArrayList<>());
            final EthLog logResult = this.web3j.ethGetLogs(filter).send();
            if (logResult.hasError()) {
                throw new IOException(logResult.getError().getMessage());
            }

            return this.transformBlockResults(blockResult, logResult);
        } catch (IOException ex) {
            final String message = String.format("Error when retrieving the data for blocknumber '%s'.", blockNumber);
            LOGGER.log(Level.SEVERE, message, ex);
            throw ex;
        }
    }

    private EthereumBlock transformBlockResults(EthBlock blockResult, EthLog logResult) {
        final EthereumBlock ethBlock = this.createEthereumBlock(blockResult.getBlock());
        this.addTransactions(ethBlock, blockResult.getBlock());
        this.addLogs(ethBlock, logResult);
        return ethBlock;
    }

    private EthereumBlock createEthereumBlock(Block block) {
        return new EthereumBlock(
            block.getNumberRaw(), 
            block.getHash(), 
            block.getParentHash(),
            block.getNonceRaw(),
            block.getSha3Uncles(),
            block.getLogsBloom(),
            block.getTransactionsRoot(),
            block.getStateRoot(),
            block.getReceiptsRoot(),
            block.getMiner(),
            block.getDifficultyRaw(),
            block.getTotalDifficultyRaw(),
            block.getExtraData(),
            block.getSizeRaw(), 
            block.getGasLimitRaw(),
            block.getGasUsedRaw(), 
            block.getTimestampRaw()
        );
    }

    private void addTransactions(EthereumBlock ethBlock, Block block) {
        for (int i = 0; i < block.getTransactions().size(); i++) {
            final Transaction tx = (Transaction)block.getTransactions().get(i);
            addEthereumTransaction(ethBlock, tx);           
        }
    }

    private void addEthereumTransaction(EthereumBlock block, Transaction tx) {
        final EthereumTransaction ethTx = new EthereumTransaction(
            block, 
            tx.getFrom(), 
            tx.getGasRaw(), 
            tx.getGasPriceRaw(), 
            tx.getHash(), 
            tx.getInput(), 
            tx.getNonceRaw(), 
            tx.getTo(), 
            tx.getTransactionIndexRaw(), 
            tx.getValueRaw(), 
            tx.getV(), 
            tx.getR(), 
            tx.getS()
        );
        block.addTransaction(ethTx);
    }

    private void addLogs(EthereumBlock ethBlock, EthLog logResult) {
        for (int i = 0; i < logResult.getLogs().size(); i++) {
            final Log log = (Log)logResult.getLogs().get(i);
            this.addLog(ethBlock, log);
        }
    }

    private void addLog(EthereumBlock ethBlock, Log log) {
        final EthereumTransaction tx = ethBlock.transactionStream().filter(t -> t.getHash().equals(log.getTransactionHash())).findAny().orElse(null);
        if (tx == null) {
            LOGGER.log(Level.WARNING, String.format("Couldn't find transaction with hash '%s'.", log.getTransactionHash()));
            // TODO: return error notification
            return;
        }

        final EthereumLog ethLog = new EthereumLog(
            tx, 
            log.isRemoved(), 
            log.getAddress(), 
            log.getData(), 
            log.getTopics()
        );

        tx.addLog(ethLog);
    }

    
}