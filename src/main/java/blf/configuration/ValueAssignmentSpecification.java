package blf.configuration;

import blf.core.instructions.VariableAssignmentInstruction;
import io.reactivex.annotations.NonNull;

/**
 * ValueAssignmentSpecification
 */
public class ValueAssignmentSpecification extends InstructionSpecification<VariableAssignmentInstruction> {

    private ValueAssignmentSpecification(VariableAssignmentInstruction assignment) {
        super(assignment);
    }

    public static ValueAssignmentSpecification of(
        @NonNull ValueMutatorSpecification mutator,
        @NonNull ValueAccessorSpecification accessor
    ) {
        return new ValueAssignmentSpecification(new VariableAssignmentInstruction(mutator.getMutator(), accessor.getValueAccessor()));
    }

}
