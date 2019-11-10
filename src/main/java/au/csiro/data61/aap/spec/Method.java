package au.csiro.data61.aap.spec;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Method
 */
public class Method {
    private final MethodSignature signature;
    private final Function<Object[], Object> implementation;
    
    public Method(
        Function<Object[], Object> implementation, 
        SolidityType returnType, 
        String methodName, 
        SolidityType... parameterTypes
    ) {
        assert implementation != null;
        assert methodName != null;
        assert returnType != null;
        assert Arrays.stream(parameterTypes).allMatch(Objects::nonNull);
        this.signature = new MethodSignature(returnType, methodName, parameterTypes);
        this.implementation = implementation;
    }

    public Method(Function<Object[], Object> implementation, MethodSignature signature) {
        assert implementation != null;
        assert signature != null;
        this.signature = signature;
        this.implementation = implementation;
    }

    public MethodSignature getSignature() {
        return this.signature;
    }

    public MethodResult<Object> execute(Object[] parameters) {
        try {
            final Object result = this.implementation.apply(parameters);
            return MethodResult.ofResult(result);
        }
        catch (Throwable errorCause) {
            return MethodResult.ofError(String.format("Error executing method '%s' with parameters (%s)", this.signature.getName(), this.createParameterList(parameters)), errorCause);
        }
    }

    private String createParameterList(Object[] parameters) {
        return Arrays.stream(parameters)
            .map(p -> p == null ? "null" : p.toString())
            .collect(Collectors.joining(", "));
    }
}