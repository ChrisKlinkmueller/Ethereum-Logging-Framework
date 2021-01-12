package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;

public class EthereumConnectInstruction implements Instruction {
    @Override
    public void execute(ProgramState state) throws ProgramException {
        EthereumProgramState ethereumState = (EthereumProgramState) state;

        ethereumState.getReader().connect(ethereumState.connectionUrl);
    }

}
