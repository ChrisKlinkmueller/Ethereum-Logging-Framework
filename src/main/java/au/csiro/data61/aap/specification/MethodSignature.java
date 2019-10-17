package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.specification.types.SolidityType;

/**
 * MethodSignature
 */
public class MethodSignature {
    private final String name;
    private final SolidityType<?> returnType;
    private final SolidityType<?>[] parameterTypes;
    
    public MethodSignature(String name, SolidityType<?> returnType, SolidityType<?>... parameterTypes) {
        assert name != null && !name.trim().isEmpty();
        assert returnType != null;
        assert Arrays.stream(parameterTypes).allMatch(p -> p != null);
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {
        return this.name;
    }

    public SolidityType<?> getReturnType() {
        return this.returnType;
    }

    public int parameterTypeCount() {
        return this.parameterTypes.length;
    }

    public SolidityType<?> getParameterType(int index) {
        assert 0 <= index && index < this.parameterTypeCount();
        return this.parameterTypes[index];
    }

    public Stream<SolidityType<?>> parameterTypeStream() {
        return Arrays.stream(this.parameterTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MethodSignature)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        final MethodSignature signature = (MethodSignature)obj;
        if (!this.name.equals(signature.name) || !this.returnType.equals(signature.returnType)) {
            return false;
        }

        return this.parameterListEquals(signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.returnType, this.parameterTypes);
    }

    @Override
    public String toString() {
        return String.format("%s %s(%s)",
            this.returnType.toString(),
            this.name,
            this.parameterTypeStream().map(p -> p.toString()).collect(Collectors.joining(", "))
        );
    }

	public boolean parameterListEquals(MethodSignature signature) {
        if (this.parameterTypeCount() != signature.parameterTypeCount()) {
            return false;
        }
    
        return IntStream.range(0, this.parameterTypeCount())
            .allMatch(i -> this.parameterTypes[i].equals(signature.parameterTypes[i]));
	}
}