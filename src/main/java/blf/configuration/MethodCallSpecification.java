package blf.configuration;

import blf.core.instructions.MethodCallInstruction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MethodCallSpecification
 */
public class MethodCallSpecification extends InstructionSpecification<MethodCallInstruction> {

    private MethodCallSpecification(MethodCallInstruction call) {
        super(call);
    }

    public static MethodCallSpecification of(MethodSpecification specification, ValueAccessorSpecification... accessors) {
        return of(specification, Arrays.asList(accessors));
    }

    public static MethodCallSpecification of(
        MethodSpecification specification,
        ValueMutatorSpecification mutator,
        ValueAccessorSpecification... accessors
    ) {
        return of(specification, mutator, Arrays.asList(accessors));
    }

    public static MethodCallSpecification of(MethodSpecification specification, List<ValueAccessorSpecification> accessors) {
        return of(specification, null, accessors);
    }

    public static MethodCallSpecification of(
        MethodSpecification specification,
        ValueMutatorSpecification mutator,
        List<ValueAccessorSpecification> accessors
    ) {
        return new MethodCallSpecification(
            new MethodCallInstruction(
                specification.getMethod(),
                accessors.stream().map(ValueAccessorSpecification::getValueAccessor).collect(Collectors.toList()),
                mutator == null ? null : mutator.getMutator()
            )
        );
    }
}
