package blf.blockchains.ethereum.reader;

import blf.core.exceptions.ExceptionHandler;
import io.reactivex.annotations.NonNull;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Web3jClient
 */
public class Web3jClient implements EthereumClient {
    private static final Logger LOGGER = Logger.getLogger(Web3jClient.class.getName());

    private final Service service;
    private final WebSocketService wsService;
    private final Web3j web3j;

    private final ExceptionHandler exceptionHandler;

    private Web3jClient(WebSocketService wsService) {
        this.wsService = wsService;
        this.service = null;
        this.web3j = Web3j.build(wsService);

        this.exceptionHandler = new ExceptionHandler();
    }

    private Web3jClient(Service service) {
        this.service = service;
        this.wsService = null;
        this.web3j = Web3j.build(service);

        this.exceptionHandler = new ExceptionHandler();
    }

    // TODO: probably not the best solution to have a static function -> rework to make it non-static
    public static Web3jClient connectWebsocket(String url) {
        final ExceptionHandler exceptionHandler = new ExceptionHandler();

        try {
            final WebSocketClient wsClient = new WebSocketClient(new URI(url));
            final WebSocketService wsService = new WebSocketService(wsClient, false);

            wsService.connect();

            return new Web3jClient(wsService);
        } catch (Exception e) {
            exceptionHandler.handleException(e.getMessage(), e);
        }

        return null;
    }

    // TODO: probably not the best solution to have a static function -> rework to make it non-static
    public static Web3jClient connectIpc(String path) {

        final ExceptionHandler exceptionHandler = new ExceptionHandler();

        final Service service = createIpcService(path);

        if (service == null) {
            final String errorMsg = String.format("Ipc connection not for %s operating system.", osName());
            exceptionHandler.handleException(errorMsg, new ConnectException());

            return null;
        }

        return new Web3jClient(service);
    }

    private static Service createIpcService(String path) {
        if (isWindowsOS()) {
            return new WindowsIpcService(path);
        } else if (isUnixOs() || isLinuxOs()) {
            return new UnixIpcService(path);
        } else {
            return null;
        }
    }

    private static boolean isWindowsOS() {
        return osName().contains("win");
    }

    private static boolean isUnixOs() {
        return osName().contains("nix");
    }

    private static boolean isLinuxOs() {
        return osName().contains("linux");
    }

    private static String osName() {
        return System.getProperty("os.name").toLowerCase();
    }

    public void close() {
        if (this.wsService != null) {
            this.wsService.close();
        } else if (this.service != null) {
            try {
                this.service.close();
            } catch (IOException ex) {
                final String message = "Error when closing Web3j service.";
                LOGGER.log(Level.SEVERE, message, ex);
            }
        }
    }

    public BigInteger queryBlockNumber() {

        final EthBlockNumber queryResult;
        try {
            queryResult = this.web3j.ethBlockNumber().send();

            if (queryResult.hasError()) {
                this.exceptionHandler.handleException(queryResult.getError().getMessage(), new IOException());

                return null;
            }

            return queryResult.getBlockNumber();

        } catch (IOException e) {
            this.exceptionHandler.handleException(e.getMessage(), e);
        }

        return null;
    }

    @SuppressWarnings("all")
    public List<Type> queryPublicMember(
        String contract,
        BigInteger block,
        String memberName,
        List<Type> inputParameters,
        List<TypeReference<?>> returnTypes
    ) throws IOException {
        assert contract != null;
        assert block != null;
        assert memberName != null;
        assert inputParameters != null && inputParameters.stream().allMatch(Objects::nonNull);
        assert returnTypes != null && returnTypes.stream().allMatch(Objects::nonNull);
        Function function = new Function(memberName, inputParameters, returnTypes);
        String data = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.request.Transaction tx = org.web3j.protocol.core.methods.request.Transaction
            .createEthCallTransaction(contract, contract, data);
        final DefaultBlockParameterNumber number = new DefaultBlockParameterNumber(block);
        EthCall result = this.web3j.ethCall(tx, number).send();
        return FunctionReturnDecoder.decode(
            result.getResult(),
            returnTypes.stream().map(t -> (TypeReference<Type>) t).collect(Collectors.toList())
        );
    }

    public EthereumBlock queryBlockData(BigInteger blockNumber) throws IOException {
        final DefaultBlockParameterNumber number = new DefaultBlockParameterNumber(blockNumber);

        final EthBlock blockResult = this.web3j.ethGetBlockByNumber(number, true).send();
        if (blockResult.hasError()) {
            this.exceptionHandler.handleException(blockResult.getError().getMessage(), new IOException());

            return null;
        }

        final EthFilter filter = new EthFilter(number, number, new ArrayList<>());
        final EthLog logResult = this.web3j.ethGetLogs(filter).send();
        if (logResult.hasError()) {
            this.exceptionHandler.handleException(logResult.getError().getMessage(), new IOException());

            return null;
        }

        return this.transformBlockResults(blockResult, logResult);

    }

    TransactionReceipt queryTransactionReceipt(String hash) {
        final EthGetTransactionReceipt transactionReceipt;
        try {
            transactionReceipt = this.web3j.ethGetTransactionReceipt(hash).send();
        } catch (IOException e) {
            this.exceptionHandler.handleException(e.getMessage(), e);

            return null;
        }

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
            final Transaction tx = (Transaction) block.getTransactions().get(i);
            addEthereumTransaction(ethBlock, tx);
        }
    }

    private void addEthereumTransaction(EthereumBlock block, Transaction tx) {
        final EthereumTransaction ethTx = new Web3jTransaction(this, block, tx);
        block.addTransaction(ethTx);
    }

    private void addLogs(EthereumBlock ethBlock, EthLog logResult) {
        for (int i = 0; i < logResult.getLogs().size(); i++) {
            final Log log = (Log) logResult.getLogs().get(i);
            this.addLog(ethBlock, log);
        }
    }

    private void addLog(EthereumBlock ethBlock, @NonNull Log log) {
        final EthereumTransaction tx = ethBlock.transactionStream()
            .filter(t -> t.getHash().equals(log.getTransactionHash()))
            .findAny()
            .orElse(null);
        if (tx == null) {
            LOGGER.log(Level.WARNING, "Could not find transaction with hash {0}.", log.getTransactionHash());
            return;
        }

        final EthereumLogEntry ethLog = new Web3jLogEntry(tx, log);
        tx.addLog(ethLog);
    }

}
