package blf.blockchains.ethereum.state;

import blf.blockchains.ethereum.reader.*;
import blf.core.state.ProgramState;

import java.util.logging.Logger;

public class EthereumProgramState extends ProgramState {

    private static final Logger LOGGER = Logger.getLogger(EthereumProgramState.class.getName());

    private final EthereumDataReader reader;

    public String connectionUrl = "";
    public String connectionIpcPath = "";

    public EthereumProgramState() {
        super();
        this.reader = new EthereumDataReader();
    }

    public EthereumDataReader getReader() {
        return this.reader;
    }

    @Override
    public void close() {
        this.getReader().close();
    }
}
