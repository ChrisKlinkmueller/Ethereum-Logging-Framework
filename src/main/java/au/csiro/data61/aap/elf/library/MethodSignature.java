package au.csiro.data61.aap.elf.library;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import au.csiro.data61.aap.elf.types.Type;

public final class MethodSignature {
    private final String name;
    private final List<Type> parameterTypes;

    public MethodSignature(String name, Type... parameterTypes) {
        this(name, List.of(parameterTypes));
    }

    public MethodSignature(String name, List<Type> parameterTypes) {
        checkNotNull(name);
        checkArgument(!name.isBlank());
        checkNotNull(parameterTypes);
        parameterTypes.forEach(pt -> checkNotNull(pt));

        this.name = name;
        this.parameterTypes = List.copyOf(parameterTypes);
    }

    public String getName() {
        return name;
    }

    public List<Type> getParameterTypes() {
        return this.parameterTypes;
    }

    public boolean isAssignableFrom(MethodSignature signature) {
        checkNotNull(signature);

        if (!this.name.equals(signature.name)) {
            return false;
        }

        return this.compareParameters(signature, Type::isAssignableFrom);
    }

    private boolean compareParameters(MethodSignature signature, BiPredicate<Type, Type> comparison) {
        checkNotNull(signature);

        final int size = this.parameterTypes.size();
        if (size != signature.parameterTypes.size()) {
            return false;
        }

        return IntStream.range(0, size)
            .allMatch(i -> comparison.test(this.parameterTypes.get(i), signature.parameterTypes.get(i)));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodSignature)) {
            return false;
        }

        final MethodSignature signature = (MethodSignature)obj;
        if (!this.name.equals(signature.name)) {
            return false;
        }

        return this.compareParameters(signature, Type::equals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.parameterTypes);
    }

    @Override
    public String toString() {
        final String parameterList = this.parameterTypes.stream()
            .map(Type::toString)
            .collect(Collectors.joining(", "));        
        return String.format("%s(%s)", this.name, parameterList);
    }
}
