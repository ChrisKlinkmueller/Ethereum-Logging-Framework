package au.csiro.data61.aap.elf.core;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.values.ValueMutator;

/**
 * VariableDefinition
 */
public class VariableAssignment implements Instruction {
    private final ValueMutator valueMutator;
    private final ValueAccessor valueAccessor;

    public VariableAssignment(ValueMutator valueMutator, ValueAccessor valueAccessor) {
        assert valueMutator != null;
        assert valueAccessor != null;
        this.valueMutator = valueMutator;
        this.valueAccessor = valueAccessor;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final Object value = this.valueAccessor.getValue(state);
        this.valueMutator.setValue(value, state);
    }    
}