package au.csiro.data61.aap.elf.core.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;

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