package au.csiro.data61.aap.etl.configuration;

import au.csiro.data61.aap.etl.core.Instruction;

/**
 * InstructionBuilder
 */
public interface InstructionSpecification {
    public Instruction getInstruction();    
}