package au.csiro.data61.aap.generation;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import au.csiro.data61.aap.spec.Scope;
import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * VariableGenerator
 */
public class VariableGenerator {
    private static final String NAME_FIRST_LETTER = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NAME_LETTERS = NAME_FIRST_LETTER + "0123456789";

    private final Random random;  
    
    public VariableGenerator(Random random) {
        assert random != null;
        this.random = random;
    }

    public Variable getBlockVariable(Scope scope) {
        assert scope != null;
        return this.getBlockVariable(scope, variable -> true);
    }

    public Variable getBlockVariable(Scope scope, SolidityType variableType) {
        assert scope != null;
        assert variableType != null;
        return this.getBlockVariable(scope, variable -> variable.getType().conceptuallyEquals(variableType));
    }

    private Variable getBlockVariable(Scope scope, Predicate<Variable> variableSelection) {
        final List<Variable> variables = scope.defaultVariableStream()
            .filter(variableSelection)
            .collect(Collectors.toList());
        return variables.isEmpty() ? null : variables.get(this.random.nextInt(variables.size()));
    }

    public Variable getVariable(SolidityType type) {
        return new Variable(type, this.generateVariableName());
    }

    private String generateVariableName() {
        final String firstPart = this.generateVariableNamePart();

        if (this.random.nextBoolean()) {
            return firstPart;
        }

        return String.format(
            "%s:%s",
            firstPart,
            this.generateVariableNamePart()
        );
    }

    private String generateVariableNamePart() {
        return String.format(
            "%s%s",
            GeneratorUtils.generateString(NAME_FIRST_LETTER, 1),
            GeneratorUtils.generateString(NAME_LETTERS, 1 + this.random.nextInt(10))
        );
    }
}