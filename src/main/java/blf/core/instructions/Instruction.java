package blf.core.instructions;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;

import java.util.LinkedList;
import java.util.List;

/**
 * An abstract class that services as a base class for every possible instruction.
 */
public abstract class Instruction {

    private final List<Instruction> nestedInstructions;

    protected Instruction() {
        this.nestedInstructions = new LinkedList<>();
    }

    protected Instruction(final List<Instruction> nestedInstructions) {

        nestedInstructions.forEach(instruction -> {
            if (instruction == null) {
                ExceptionHandler.getInstance().handleException("Nested instruction can not be null.", new NullPointerException());
            }
        });

        this.nestedInstructions = new LinkedList<>(nestedInstructions);
    }

    /**
     * Executes the main functionality of the instructions.
     * By default a particular instruction instance does nothing.
     *
     * @param programState The current ProgramState of the BLF.
     */
    public void execute(final ProgramState programState) {}

    /**
     * Executes the nested instructions (in most of the cases EMIT instructions).
     *
     * @param programState The current ProgramState of the BLF.
     */
    public void executeNestedInstructions(final ProgramState programState) {
        for (Instruction nestedInstruction : this.nestedInstructions) {
            nestedInstruction.execute(programState);
        }
    }
}
