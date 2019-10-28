package au.csiro.data61.aap.spec;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Method
 */
public class Method {
    private final SolidityType returnType;
    private final SolidityType[] parameterTypes;
    private final String name;
    private final Function<Object[], MethodResult<Object>> implementation;
    
    public Method(Function<Object[], MethodResult<Object>> implementation, SolidityType returnType, String name, SolidityType... parameterTypes) {
        assert implementation != null;
        assert returnType != null;
        assert name != null && !name.trim().isEmpty();
        assert Arrays.stream(parameterTypes).allMatch(Objects::nonNull);
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length);
        this.implementation = implementation;
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

    public MethodResult<Object> execute(Object[] parameters) {
        try {
            return this.implementation.apply(parameters);
        }
        catch (Throwable errorCause) {
            return MethodResult.ofError(String.format("Error executing method '%s' with parameters ('%s')", this.name, this.createParameterList(parameters)), errorCause);
        }
    }

    private String createParameterList(Object[] parameters) {
        return Arrays.stream(parameters)
            .map(p -> p == null ? "null" : p.toString())
            .collect(Collectors.joining(", "));
    }
}