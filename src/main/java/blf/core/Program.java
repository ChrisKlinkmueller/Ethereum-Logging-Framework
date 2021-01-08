package blf.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import blf.core.exceptions.ProgramException;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;

/**
 * Program
 */
public class Program {

    private final List<Instruction> instructions;

    public Program(FilterInstruction... instructions) {
        this(Arrays.asList(instructions));
    }

    public Program(List<Instruction> instructions) {
        assert instructions.stream().allMatch(Objects::nonNull);
        this.instructions = new LinkedList<>(instructions);
    }

    public void execute(ProgramState state) {
        try {
            this.executeInstructions(state);
            state.getWriters().writeAllData();
        } catch (final Throwable ex) {
            final String message = "Error when executing the program.";
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, ex);
        }
        // finally {
        // state.close();
        // }
    }

    protected void executeInstructions(ProgramState state) throws ProgramException {
        for (Instruction instruction : this.instructions) {
            instruction.execute(state);
        }
    }

}
