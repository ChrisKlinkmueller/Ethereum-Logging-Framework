package au.csiro.data61.aap.etl.core;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    public void execute(ProgramState state) throws ProgramException;
}