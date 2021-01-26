package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;

public class EthereumConnectIpcInstruction extends Instruction {
    @Override
    public void execute(ProgramState state) {
        EthereumProgramState ethereumState = (EthereumProgramState) state;

        try {
            ethereumState.getReader().connectIpc(ethereumState.connectionUrl);
        } catch (ProgramException e) {
            // TODO: remove the throw of ProgramException
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(e.getMessage(), e);
        }
    }
}
