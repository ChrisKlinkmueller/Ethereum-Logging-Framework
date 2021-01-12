package blf.core.instructions;

import blf.core.exceptions.ProgramException;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;
import io.reactivex.annotations.NonNull;

/**
 * VariableDefinition
 */
public class VariableAssignmentInstruction implements Instruction {
    private final ValueMutator valueMutator;
    private final ValueAccessor valueAccessor;

    public VariableAssignmentInstruction(@NonNull ValueMutator valueMutator, @NonNull ValueAccessor valueAccessor) {
        this.valueMutator = valueMutator;
        this.valueAccessor = valueAccessor;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final Object value = this.valueAccessor.getValue(state);
        this.valueMutator.setValue(value, state);
    }
}
