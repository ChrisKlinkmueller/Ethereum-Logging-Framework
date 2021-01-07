package blf.core.filters;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * SmartContractQuery
 */
public interface EthereumSmartContractQuery {
    public void query(String contract, EthereumProgramState state) throws ProgramException;
}
