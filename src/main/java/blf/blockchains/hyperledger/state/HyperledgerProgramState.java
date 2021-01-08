package blf.blockchains.hyperledger.state;

import blf.blockchains.hyperledger.reader.HyperledgerDataReader;
import blf.core.ProgramState;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;

public class HyperledgerProgramState extends ProgramState {

    public HyperledgerProgramState() {
        super();
        this.reader = new HyperledgerDataReader();
    }

    public void close() {
        this.getReader().close();
    }

    private final HyperledgerDataReader reader;

    public HyperledgerDataReader getReader() {
        return this.reader;
    }

    private String networkConfigFilePath;

    public String getNetworkConfigFilePath() {
        return networkConfigFilePath;
    }

    public void setNetworkConfigFilePath(String networkConfigFilePath) {
        this.networkConfigFilePath = networkConfigFilePath;
    }

    private String serverKeyFilePath;

    public String getServerKeyFilePath() {
        return serverKeyFilePath;
    }

    public void setServerKeyFilePath(String serverKeyFilePath) {
        this.serverKeyFilePath = serverKeyFilePath;
    }

    private String serverCrtFilePath;

    public String getServerCrtFilePath() {
        return serverCrtFilePath;
    }

    public void setServerCrtFilePath(String serverCrtFilePath) {
        this.serverCrtFilePath = serverCrtFilePath;
    }

    private String mspName;

    public String getMspName() {
        return mspName;
    }

    public void setMspName(String mspName) {
        this.mspName = mspName;
    }

    private String channel;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    private Gateway gateway;

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    private Network network;

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

}
