package blf.core.instructions;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;
import blf.core.writers.DataWriters;

import java.math.BigInteger;
import java.util.List;

public class BlockInstruction extends Instruction {

    protected BlockInstruction(final List<Instruction> nestedInstructions) {
        super(nestedInstructions);
    }

    @Override
    public void executeNestedInstructions(final ProgramState programState) {
        BigInteger currentBlockNumber = (BigInteger) programState.getBlockchainVariables()
            .currentBlockNumberAccessor()
            .getValue(programState);

        if (currentBlockNumber == null) {
            ExceptionHandler.getInstance().handleException("Current block number is null.", new NullPointerException());

            return;
        }

        final DataWriters dataWriters = programState.getWriters();

        dataWriters.startNewBlock(currentBlockNumber);

        super.executeNestedInstructions(programState);

        dataWriters.writeBlock();
    }
}
