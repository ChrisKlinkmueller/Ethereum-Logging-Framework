package au.csiro.data61.aap.spec;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import au.csiro.data61.aap.util.MethodResult;

/**
 * Method
 */
public class Method {
    private final MethodSignature signature;
    private final Function<Object[], MethodResult<Object>> implementation;
    
    public Method(Function<Object[], MethodResult<Object>> implementation, MethodSignature signature) {
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
            return this.implementation.apply(parameters);
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