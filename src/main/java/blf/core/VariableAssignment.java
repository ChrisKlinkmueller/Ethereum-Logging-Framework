package blf.core;

import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;
import io.reactivex.annotations.NonNull;

/**
 * VariableDefinition
 */
public class VariableAssignment implements Instruction {
    private final ValueMutator valueMutator;
    private final ValueAccessor valueAccessor;

    public VariableAssignment(@NonNull ValueMutator valueMutator, @NonNull ValueAccessor valueAccessor) {
        this.valueMutator = valueMutator;
        this.valueAccessor = valueAccessor;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final Object value = this.valueAccessor.getValue(state);
        this.valueMutator.setValue(value, state);
    }
}
