package au.csiro.data61.aap.program;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.program.types.ValueCasts;
import au.csiro.data61.aap.util.MethodResult;

/**
 * MethodCall
 */
public class MethodCall implements ValueSource, Executable {
    private static final Logger LOGGER = Logger.getLogger(MethodCall.class.getName());
    private final Method method;
    private final Variable[] parameters;
    private MethodResult<Object> result;

    public MethodCall(Method method, Variable... parameters) {
        assert method != null;
        assert Arrays.stream(parameters).allMatch(Objects::nonNull);
        assert method.getSignature().parameterTypeCount() == parameters.length;
        assert IntStream.range(0, parameters.length)
                .allMatch(i -> ValueCasts.isCastSupported(parameters[i].getType(), method.getSignature().getParameterType(i)));
        this.method = method;
        // TODO: if a variable type is not equal to the respective parameter type, 
        //       wrap the variable in a cast
        this.parameters = Arrays.copyOf(parameters, parameters.length);
    }

    public Method getMethod() {
        return this.method;
    }

    public int parameterCount() {
        return this.parameters.length;
    }

    public Variable getParameter(int index) {
        assert 0 <= index && index < this.parameterCount();
        return this.parameters[index];
    }

    public Stream<Variable> parameterStream() {
        return Arrays.stream(this.parameters);
    }

    @Override
    public Object getValue() {
       return this.result;
    }

    @Override
    public SolidityType getType() {
        return this.method.getSignature().getReturnType();
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        final Object[] parameters = this.parameterStream().map(variable -> variable.getValue()).toArray();
        
        try {
            this.result = this.method.execute(state, parameters);
            return MethodResult.ofResult();
        }
        catch (Throwable cause) {
            this.result = null;
            LOGGER.log(Level.SEVERE, "Failed method execution.", cause);
            final String message = String.format(
                "Failed execution of method '%s' with parameters: %s.", 
                this.method.getSignature().getName(),
                Arrays.stream(parameters).map(param -> param.toString()).collect(Collectors.joining(", "))
            );
            return MethodResult.ofError(message, cause);
        }
    }
}