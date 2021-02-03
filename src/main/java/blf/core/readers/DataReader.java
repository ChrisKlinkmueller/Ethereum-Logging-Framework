package blf.core.readers;

public abstract class DataReader<T1, T2, T3, T4> {
    protected T1 client;
    protected T2 currentBlock;
    protected T3 currentTransaction;
    protected T4 currentLogEntry;

    public T1 getClient() {
        return this.client;
    }

    public T2 getCurrentBlock() {
        return this.currentBlock;
    }

    public void setCurrentBlock(T2 currentBlock) {
        this.currentBlock = currentBlock;
    }

    public T3 getCurrentTransaction() {
        return this.currentTransaction;
    }

    public void setCurrentTransaction(T3 currentTransaction) {
        this.currentTransaction = currentTransaction;
    }

    public T4 getCurrentLogEntry() {
        return this.currentLogEntry;
    }

    public void setCurrentLogEntry(T4 currentLogEntry) {
        this.currentLogEntry = currentLogEntry;
    }

    public void connect(Object[] parameters) {}

    public void connectIpc(String path) {}

    public void close() {}
}
