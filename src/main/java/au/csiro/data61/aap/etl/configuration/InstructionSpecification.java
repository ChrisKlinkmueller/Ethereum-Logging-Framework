package au.csiro.data61.aap.etl.configuration;

import au.csiro.data61.aap.etl.core.Instruction;

/**
 * InstructionBuilder
 */
public abstract class InstructionSpecification {
    private final Instruction instruction;

    protected InstructionSpecification(Instruction instruction) {
        assert instruction != null;
        this.instruction = instruction;
    }

    public Instruction getInstruction() {
        return this.instruction;
    }
}