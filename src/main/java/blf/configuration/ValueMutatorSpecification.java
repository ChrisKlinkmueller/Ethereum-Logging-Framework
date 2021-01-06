package blf.configuration;

import blf.core.values.ValueMutator;
import blf.core.values.Variables;
import io.reactivex.annotations.NonNull;

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

    public static ValueMutatorSpecification ofVariableName(@NonNull String name) {
        ValueMutator mutator = Variables.createValueMutator(name);
        return new ValueMutatorSpecification(mutator);
    }

}
