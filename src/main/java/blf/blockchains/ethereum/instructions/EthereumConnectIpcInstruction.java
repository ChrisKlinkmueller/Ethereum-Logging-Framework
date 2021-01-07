package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import io.reactivex.annotations.NonNull;

public class EthereumConnectIpcInstruction implements Instruction {
    @Override
    public void execute(ProgramState state) throws ProgramException {
        EthereumProgramState ethereumState = (EthereumProgramState) state;

        ethereumState.getReader().connectIpc(ethereumState.connectionUrl);
    }
}
