package au.csiro.data61.aap.etl.core.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * Scope
 */
public abstract class Filter implements Instruction {
    private final List<Instruction> instructions;

    public Filter(List<Instruction> instructions, List<Instruction> valueCreators, List<Instruction> blockValueRemovers) {
        assert instructions != null && instructions.stream().allMatch(Objects::nonNull);
        this.instructions = new LinkedList<>(valueCreators);
        this.instructions.addAll(instructions);
        this.instructions.addAll(blockValueRemovers);
    }

    protected void executeInstructions(ProgramState state) throws ProgramException {
        for (Instruction instruction : this.instructions) {
            instruction.execute(state);
        }
    }    
} 