package blf.core.instructions;

import blf.core.exceptions.ProgramException;
import blf.core.state.ProgramState;

import java.math.BigInteger;
import java.util.List;

public class BlockInstruction extends Instruction {
    protected BlockInstruction() {
        super();
    }

    protected BlockInstruction(final List<Instruction> nestedInstructions) {
        super(nestedInstructions);
    }

    @Override
    public void executeNestedInstructions(final ProgramState programState) {
        try {
            programState.getWriters()
                .startNewBlock((BigInteger) programState.getBlockchainVariables().currentBlockNumberAccessor().getValue(programState));
        } catch (ProgramException e) {
            programState.getExceptionHandler().handleExceptionAndDecideOnAbort(e.getMessage(), e);
        }
        super.executeNestedInstructions(programState);
        try {
            programState.getWriters().writeBlock();
        } catch (Throwable throwable) {
            programState.getExceptionHandler().handleExceptionAndDecideOnAbort(throwable.getMessage(), throwable);
        }
    }
}
