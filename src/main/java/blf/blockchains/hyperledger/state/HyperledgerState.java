package blf.blockchains.hyperledger.state;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;

public class HyperledgerState {
    public Gateway gateway = null;
    public Network network = null;
    public Contract contract = null;
}
