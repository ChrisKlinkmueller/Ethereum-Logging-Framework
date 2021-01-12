package blf.core.interfaces;

import blf.core.state.ProgramState;
import blf.core.exceptions.ProgramException;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    void execute(ProgramState state) throws ProgramException;
}
