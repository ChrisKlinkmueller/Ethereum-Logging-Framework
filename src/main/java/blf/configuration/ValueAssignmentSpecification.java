package blf.configuration;

import blf.core.VariableAssignment;
import io.reactivex.annotations.NonNull;

/**
 * ValueAssignmentSpecification
 */
public class ValueAssignmentSpecification extends InstructionSpecification<VariableAssignment> {

    private ValueAssignmentSpecification(VariableAssignment assignment) {
        super(assignment);
    }

    public static ValueAssignmentSpecification of(
        @NonNull ValueMutatorSpecification mutator,
        @NonNull ValueAccessorSpecification accessor
    ) {
        return new ValueAssignmentSpecification(new VariableAssignment(mutator.getMutator(), accessor.getValueAccessor()));
    }

}
