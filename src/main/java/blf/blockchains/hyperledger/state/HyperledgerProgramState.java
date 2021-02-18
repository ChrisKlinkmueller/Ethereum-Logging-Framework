package blf.blockchains.hyperledger.state;

import blf.blockchains.hyperledger.variables.HyperledgerVariables;
import blf.core.state.ProgramState;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.sdk.BlockEvent;

import java.math.BigInteger;

public class HyperledgerProgramState extends ProgramState {

    public HyperledgerProgramState() {
        super(new HyperledgerVariables());
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

    // ======= msp name ======
    private String mspName;

    public String getMspName() {
        return mspName;
    }

    public void setMspName(String mspName) {
        this.mspName = mspName;
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
    private BlockEvent currentBlock;

    public BlockEvent getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(BlockEvent currentBlock) {
        this.currentBlock = currentBlock;
    }
}
