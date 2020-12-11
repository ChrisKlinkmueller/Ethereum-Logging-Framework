package blf.configuration;

import blf.core.VariableAssignment;

/**
 * ValueAssignmentSpecification
 */
public class ValueAssignmentSpecification extends InstructionSpecification<VariableAssignment> {

    private ValueAssignmentSpecification(VariableAssignment assignment) {
        super(assignment);
    }

    public static ValueAssignmentSpecification of(ValueMutatorSpecification mutator, ValueAccessorSpecification accessor) {
        assert mutator != null;
        assert accessor != null;
        return new ValueAssignmentSpecification(new VariableAssignment(mutator.getMutator(), accessor.getValueAccessor()));
    }

}
