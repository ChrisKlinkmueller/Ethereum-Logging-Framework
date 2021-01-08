package blf.blockchains.hyperledger.state;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.blockchains.hyperledger.reader.HyperledgerDataReader;
import blf.core.ProgramState;

import java.util.logging.Logger;

public class HyperledgerProgramState extends ProgramState {

    private static final Logger LOGGER = Logger.getLogger(EthereumProgramState.class.getName());

    private final HyperledgerDataReader reader;

    public HyperledgerProgramState() {
        super();
        this.reader = new HyperledgerDataReader();
    }

    public HyperledgerDataReader getReader() {
        return this.reader;
    }

    public void close() {
        this.getReader().close();
    }
}
