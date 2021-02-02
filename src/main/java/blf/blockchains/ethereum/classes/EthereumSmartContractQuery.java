package blf.blockchains.ethereum.classes;

import blf.blockchains.ethereum.state.EthereumProgramState;

/**
 * SmartContractQuery
 */
public interface EthereumSmartContractQuery {
    public void query(String contract, EthereumProgramState state);
}
