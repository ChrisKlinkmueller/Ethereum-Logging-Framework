package blf.configuration;

import blf.core.values.ValueMutator;
import blf.core.values.Variables;

/**
 * ValueMutatorSpecification
 */
public class ValueMutatorSpecification {
    private final ValueMutator mutator;

    private ValueMutatorSpecification(ValueMutator mutator) {
        this.mutator = mutator;
    }

    ValueMutator getMutator() {
        return mutator;
    }

    public static ValueMutatorSpecification ofVariableName(String name) {
        assert name != null && !name.isBlank();
        ValueMutator mutator = Variables.createValueMutator(name);
        return new ValueMutatorSpecification(mutator);
    }

}
