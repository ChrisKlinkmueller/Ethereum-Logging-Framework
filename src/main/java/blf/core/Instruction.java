package blf.core;

import blf.core.exceptions.ProgramException;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    void execute(ProgramState state) throws ProgramException;
}
