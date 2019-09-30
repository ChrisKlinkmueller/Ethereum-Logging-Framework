package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.Stream;

import au.csiro.data61.aap.library.types.SolidityType;

/**
 * MethodCall
 */
public class MethodCall implements ValueSource {
    private final MethodSignature method;
    private final Variable[] variables;

    public MethodCall(MethodSignature method, Variable[] variables) {
        assert method != null;
        assert variables != null && Arrays.stream(variables).allMatch(var -> var != null);
        this.method = method;
        this.variables = variables;
    }

    @Override
    public SolidityType<?> getReturnType() {
        return this.method.getReturnType();
    }

    public MethodSignature getMethod() {
        return this.method;
    }

    public int variableCount() {
        return this.variables.length;
    }

    public Variable getVariable(int index) {
        assert 0 <= index && index < this.variables.length;
        return this.variables[index];
    }

    public Stream<Variable> variableStream() {
        return Arrays.stream(this.variables);
    }
}