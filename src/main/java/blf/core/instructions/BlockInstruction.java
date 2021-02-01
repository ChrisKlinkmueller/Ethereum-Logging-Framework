package blf.core.instructions;

import blf.core.state.ProgramState;

import java.util.List;

public class BlockInstruction extends Instruction{
    protected BlockInstruction() { super(); }
    protected BlockInstruction(final List<Instruction> nestedInstructions) { super(nestedInstructions); }

    @Override
    public void executeNestedInstructions(final ProgramState programState) {
        super.executeNestedInstructions(programState);
        try {
            programState.getWriters().writeBlock();
        } catch (Throwable throwable) {
            programState.getExceptionHandler().handleExceptionAndDecideOnAbort(throwable.getMessage(), throwable);
        }
    }
}
