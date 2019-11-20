package au.csiro.data61.aap.etl.core;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    public void execute(ProgramState state) throws EtlException;
}