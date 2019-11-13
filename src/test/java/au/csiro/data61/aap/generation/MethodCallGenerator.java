package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.library.Library;
import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.suppliers.MethodCall;
import au.csiro.data61.aap.program.Scope;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityType;

/**
 * MethodCallGenerator
 */
public class MethodCallGenerator {
    private final Random random;

    public MethodCallGenerator(Random random) {
        assert random != null;
        this.random = random;
    }

    public MethodCall generateMethodCall(Scope scope) {
        return this.generateMethodCall(methods -> true, scope);
    }
    
    public MethodCall generateMethodCall(SolidityType returnType, Scope scope) {
        return this.generateMethodCall(
            method -> method.getSignature().getReturnType().conceptuallyEquals(returnType),
            scope
        );
    }

    private MethodCall generateMethodCall(Predicate<Method> selectionCriteria, Scope scope) {
        final Stream<Method> methods = Library.INSTANCE.methodStream()
            .filter(selectionCriteria);

        final Method method = GeneratorUtils.randomElement(methods);
        if (method == null) {
            return null;
        }

        final Variable[] variables = IntStream.range(0, method.getSignature().parameterTypeCount())
            .mapToObj(i -> this.generateParameter(scope, method.getSignature().getParameterType(i)))
            .toArray(Variable[]::new);        

        return new MethodCall(method, variables);
    }

    public Variable generateParameter(Scope scope, SolidityType type) {
        if (this.random.nextBoolean()) {
            final Stream<Variable> variableStream = scope.variableStream()
                .filter(variable -> variable.getType().conceptuallyEquals(type));

            Variable variable = GeneratorUtils.randomElement(variableStream);
            if (variable != null) {
                return variable;
            }
        }

        return GeneratorUtils.LITERAL_GENERATOR.generateLiteral(type);
    }

	public String serializeMethodCall(MethodCall source) {
        return String.format("%s(%s)", 
            source.getMethod().getSignature().getName(),
            source.parameterStream()
                .map(variable -> GeneratorUtils.VARIABLE_GENERATOR.serializeVariableReference(variable))
            .collect(Collectors.joining(", "))
        );
	}
}