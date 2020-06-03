package au.csiro.data61.aap.elf.core;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    public void execute(ProgramState state) throws ProgramException;
}
