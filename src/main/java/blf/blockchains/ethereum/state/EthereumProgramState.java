package blf.blockchains.ethereum.state;

import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.variables.EthereumVariables;
import blf.core.state.ProgramState;

public class EthereumProgramState extends ProgramState {

    private final EthereumDataReader reader;

    private String connectionUrl;
    private String connectionIpcPath;

    public EthereumProgramState() {
        super(new EthereumVariables());

        connectionUrl = "";
        connectionIpcPath = "";

        this.reader = new EthereumDataReader();
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionIpcPath() {
        return connectionIpcPath;
    }

    public void setConnectionIpcPath(String connectionIpcPath) {
        this.connectionIpcPath = connectionIpcPath;
    }

    public EthereumDataReader getReader() {
        return this.reader;
    }

    @Override
    public void close() {
        this.getReader().close();
    }
}
