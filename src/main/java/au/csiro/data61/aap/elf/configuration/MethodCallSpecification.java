package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.MethodCall;

/**
 * MethodCallSpecification
 */
public class MethodCallSpecification extends InstructionSpecification<MethodCall> {

    private MethodCallSpecification(MethodCall call) {
        super(call);
    }

    public static MethodCallSpecification of(MethodSpecification specification, ValueAccessorSpecification... accessors)
        throws BuildException {
        return of(specification, Arrays.asList(accessors));
    }

    public static MethodCallSpecification of(
        MethodSpecification specification,
        ValueMutatorSpecification mutator,
        ValueAccessorSpecification... accessors
    ) throws BuildException {
        return of(specification, mutator, Arrays.asList(accessors));
    }

    public static MethodCallSpecification of(MethodSpecification specification, List<ValueAccessorSpecification> accessors)
        throws BuildException {
        return of(specification, null, accessors);
    }

    public static MethodCallSpecification of(
        MethodSpecification specification,
        ValueMutatorSpecification mutator,
        List<ValueAccessorSpecification> accessors
    ) throws BuildException {
        assert specification != null;
        assert accessors != null && accessors.stream().allMatch(Objects::nonNull);
        return new MethodCallSpecification(
            new MethodCall(
                specification.getMethod(),
                accessors.stream().map(a -> a.getValueAccessor()).collect(Collectors.toList()),
                mutator == null ? null : mutator.getMutator()
            )
        );
    }
}
