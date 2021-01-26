package blf.core.instructions;

import blf.core.exceptions.ProgramException;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.values.ValueMutator;

/**
 * VariableDefinition
 */
public class VariableAssignmentInstruction extends Instruction {
    private final ValueMutator valueMutator;
    private final ValueAccessor valueAccessor;

    public VariableAssignmentInstruction(ValueMutator valueMutator, ValueAccessor valueAccessor) {
        this.valueMutator = valueMutator;
        this.valueAccessor = valueAccessor;
    }

    @Override
    public void execute(ProgramState state) {
        final Object value;
        try {
            value = this.valueAccessor.getValue(state);
            this.valueMutator.setValue(value, state);
        } catch (ProgramException e) {
            // TODO: remove the throw of ProgramException
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(e.getMessage(), e);
        }
    }
}
