package au.csiro.data61.aap.etl.configuration;

import au.csiro.data61.aap.etl.core.Instruction;

/**
 * InstructionBuilder
 */
public abstract class InstructionSpecification<T extends Instruction> {
    private final T instruction;

    protected InstructionSpecification(T instruction) {
        assert instruction != null;
        this.instruction = instruction;
    }

    public T getInstruction() {
        return this.instruction;
    }
}