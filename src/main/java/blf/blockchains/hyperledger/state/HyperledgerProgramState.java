package blf.blockchains.hyperledger.state;

import blf.blockchains.hyperledger.reader.HyperledgerDataReader;
import blf.core.state.ProgramState;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;

import java.math.BigInteger;

public class HyperledgerProgramState extends ProgramState {

    public HyperledgerProgramState() {
        super();
        this.reader = new HyperledgerDataReader();
    }

    public void close() {
        this.getReader().close();
    }

    // ======= reader =======
    private final HyperledgerDataReader reader;

    public HyperledgerDataReader getReader() {
        return this.reader;
    }

    // ****************************************************
    // HyperledgerConnectInstruction
    // ****************************************************

    // ======= gateway =======
    private Gateway gateway;

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    // ======= network =======
    private Network network;

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    // ****************************************************
    // HyperledgerBlockFilterInstruction
    // ****************************************************

    // ======= currentBlockNumber =======
    private BigInteger currentBlockNumber;

    public BigInteger getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public void setCurrentBlockNumber(BigInteger currentBlockNumber) {
        this.currentBlockNumber = currentBlockNumber;
    }

    // ======= currentBlock =======
    private Object currentBlock;

    public Object getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(Object currentBlock) {
        this.currentBlock = currentBlock;
    }
}
