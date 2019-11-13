package au.csiro.data61.aap.program;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Method
 */
public class Method {
    private final MethodSignature signature;
    private final MethodImplementation implementation;
    
    public Method(
        MethodImplementation implementation, 
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

    public Method(MethodImplementation implementation, MethodSignature signature) {
        assert implementation != null;
        assert signature != null;
        this.signature = signature;
        this.implementation = implementation;
    }

    public MethodSignature getSignature() {
        return this.signature;
    }

    public MethodResult<Object> execute(ProgramState state, Object[] parameters) {
        try {
            final Object result = this.implementation.execute(state, parameters);
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