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
    private final Variable[] variables;

    public MethodCall(Method method, Variable... variables) {
        assert method != null;
        assert Arrays.stream(variables).allMatch(Objects::nonNull);
        assert method.getSignature().parameterTypeCount() == variables.length;
        assert IntStream.range(0, variables.length)
                .allMatch(i -> method.getSignature().getParameterType(i).castableFrom(variables[i].getType()));
        this.method = method;
        // TODO: if a variable type is not equal to the respective parameter type, 
        //       wrap the variable in a cast
        this.variables = Arrays.copyOf(variables, variables.length);
    }

    public Method getMethod() {
        return this.method;
    }

    public int variableCount() {
        return this.variables.length;
    }

    public Variable getVariable(int index) {
        assert 0 <= index && index < this.variableCount();
        return this.variables[index];
    }

    public Stream<Variable> variableStream() {
        return Arrays.stream(this.variables);
    }

    @Override
    public MethodResult<Object> getValue() {
        final Object[] parameters = this.variableStream().map(variable -> variable.getValue().getResult()).toArray();
        return this.method.execute(parameters);
    }

    @Override
    public SolidityType getType() {
        return this.method.getSignature().getReturnType();
    }
}