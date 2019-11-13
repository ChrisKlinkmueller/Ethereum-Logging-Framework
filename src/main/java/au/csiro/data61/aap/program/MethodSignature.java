package au.csiro.data61.aap.program;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * MethodSignature
 */
public class MethodSignature {
    private final SolidityType returnType;
    private final SolidityType[] parameterTypes;
    private final String name;

    public MethodSignature(SolidityType returnType, String name, SolidityType... parameterTypes) {
        assert returnType != null;
        assert name != null && !name.isBlank();
        assert Arrays.stream(parameterTypes).allMatch(Objects::nonNull);

        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public SolidityType getReturnType() {
        return this.returnType;
    }

    public String getName() {
        return this.name;
    }

    public int parameterTypeCount() {
        return this.parameterTypes.length;
    }

    public SolidityType getParameterType(int index) {
        assert 0 <= index && index < this.parameterTypeCount();
        return this.parameterTypes[index];
    }

    public Stream<SolidityType> parameterTypeStream() {
        return Arrays.stream(this.parameterTypes);
    }    
}