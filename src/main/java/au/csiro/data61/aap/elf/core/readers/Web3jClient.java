package au.csiro.data61.aap.elf.core.readers;

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
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

/**
 * Web3jClient
 */
public class Web3jClient implements EthereumClient {
    private static final Logger LOGGER = Logger.getLogger(EthereumClient.class.getName());
    private static final String URL = "ws://localhost:8546/";

    private final WebSocketService wsService;
    private final Web3j web3j;

    public Web3jClient() throws URISyntaxException, ConnectException {
        this(URL);
    }

    public Web3jClient(String url) throws URISyntaxException, ConnectException {
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

    public BigInteger queryBlockNumber() throws Throwable {
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

    public Object ethCall(org.web3j.protocol.core.methods.request.Transaction tx, BigInteger blockNumber) throws IOException {
        return this.web3j.ethCall(tx, new DefaultBlockParameterNumber(blockNumber)).send().getResult();
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

    public void queryFunction() {
        //Type<?> type = 
        //Function function = new Function(name, inputParameters, outputParameters)
        //Transaction t = Transaction.createEthCallTransaction();
        //t.set
        //this.web3j.ethCall(transaction, defaultBlockParameter)
    }

    TransactionReceipt queryTransactionReceipt(String hash) throws IOException {
        final EthGetTransactionReceipt transactionReceipt = this.web3j.ethGetTransactionReceipt(hash).send();
        return transactionReceipt.getResult();
    }

    private EthereumBlock transformBlockResults(EthBlock blockResult, EthLog logResult) {
        final EthereumBlock ethBlock = new Web3jBlock(blockResult.getBlock());
        this.addTransactions(ethBlock, blockResult.getBlock());
        this.addLogs(ethBlock, logResult);
        return ethBlock;
    }

    private void addTransactions(EthereumBlock ethBlock, Block block) {
        for (int i = 0; i < block.getTransactions().size(); i++) {
            final Transaction tx = (Transaction)block.getTransactions().get(i);
            addEthereumTransaction(ethBlock, tx);           
        }
    }

    private void addEthereumTransaction(EthereumBlock block, Transaction tx) {
        final EthereumTransaction ethTx = new Web3jTransaction(this, block, tx);
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

        final EthereumLogEntry ethLog = new Web3jLogEntry(tx, log);
        tx.addLog(ethLog);
    }
    
}