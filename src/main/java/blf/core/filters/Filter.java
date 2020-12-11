package blf.core.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import blf.core.exceptions.ProgramException;
import blf.core.Instruction;
import blf.core.ProgramState;

/**
 * Scope
 */
public abstract class Filter implements Instruction {
    private final List<Instruction> instructions;

    public Filter(List<Instruction> instructions) {
        assert instructions != null && instructions.stream().allMatch(Objects::nonNull);
        this.instructions = new LinkedList<>(instructions);
    }

    protected void executeInstructions(ProgramState state) throws ProgramException {
        for (Instruction instruction : this.instructions) {
            instruction.execute(state);
        }
    }
}
