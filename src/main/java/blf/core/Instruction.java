package blf.core;

import blf.core.exceptions.ProgramException;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    public void execute(ProgramState state) throws ProgramException;
}
