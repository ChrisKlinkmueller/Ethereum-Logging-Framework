package au.csiro.data61.aap.program;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.csiro.data61.aap.etl.core.readers.EthereumBlock;
import au.csiro.data61.aap.etl.core.readers.EthereumClient;
import au.csiro.data61.aap.etl.core.readers.EthereumTransaction;
/**
 * ProgramState
 */
public class ProgramState implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger("Exception Handling");

    private final List<Consumer<ProgramState>> onCloseListeners;
    private EthereumBlock block;
    private EthereumTransaction transaction;
    private EthereumClient client;
    private ExceptionHandlingStrategy exceptionHandlingStrategy;

    public ProgramState() {
        this.onCloseListeners = new LinkedList<>();
        this.exceptionHandlingStrategy = ExceptionHandlingStrategy.ABORT;
    }



    @Override
    public void close() throws Exception {
        this.onCloseListeners.forEach(l -> l.accept(this));
    }

    public void addOnCloseListener(Consumer<ProgramState> listener) {
        assert listener != null;
        this.onCloseListeners.add(listener);
    }

    public void removeOnCloseListener(Consumer<ProgramState> listener) {
        assert listener != null;
        this.onCloseListeners.remove(listener);
    }



    public EthereumBlock getCurrentBlock() {
        return this.block;
    }

    public void setCurrentBlock(EthereumBlock block) {
        this.block = block;
    }

    public EthereumTransaction getCurrentTransaction() {
        return this.transaction;
    }

    public void setCurrentTransaction(EthereumTransaction transaction) {
        this.transaction = transaction;
    }

    public EthereumClient getEthereumClient() {
        return this.client;
    }

    public void setEthereumClient(EthereumClient client) {
        this.client = client;
    }


    public void setExceptionHandlingStrategy(ExceptionHandlingStrategy strategy) {
        this.exceptionHandlingStrategy = strategy;
    }

    public void reportException(String message, Throwable cause) {
        assert message != null;
        assert cause != null;
        LOGGER.log(Level.SEVERE, message, cause);
    }

    public boolean continueAfterException() {
        return this.exceptionHandlingStrategy == ExceptionHandlingStrategy.CONTINUE;
    }
}