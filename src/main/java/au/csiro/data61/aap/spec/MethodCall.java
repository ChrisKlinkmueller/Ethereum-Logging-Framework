package au.csiro.data61.aap.spec;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * MethodCall
 */
public class MethodCall implements ValueSource {
    private final Method method;
    private final Variable[] parameters;

    public MethodCall(Method method, Variable... parameters) {
        assert method != null;
        assert Arrays.stream(parameters).allMatch(Objects::nonNull);
        assert method.getSignature().parameterTypeCount() == parameters.length;
        assert IntStream.range(0, parameters.length)
                .allMatch(i -> method.getSignature().getParameterType(i).castableFrom(parameters[i].getType()));
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
    public MethodResult<Object> getValue() {
        final Object[] parameters = this.parameterStream().map(variable -> variable.getValue().getResult()).toArray();
        return this.method.execute(parameters);
    }

    @Override
    public SolidityType getType() {
        return this.method.getSignature().getReturnType();
    }
}