package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.Scope;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.suppliers.VariableCategory;
import au.csiro.data61.aap.program.types.SolidityType;

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

    public Variable selectAnyVariable(Scope scope) {
        return this.selectScopeVariable(scope.findVariablesWithinScope(v -> true), this::matchAny);  
    }

    public Variable selectAnyVariable(Scope scope, SolidityType variableType) {
        return this.selectScopeVariable(scope.findVariablesWithinScope(v -> true), this.getTypeEqualityMatcher(variableType));  
    }

    public Variable selectScopeVariable(Scope scope) {
        assert scope != null;
        return this.selectScopeVariable(scope.variableStream(), this::matchAny);
    }

    public Variable selectScopeVariable(Scope scope, SolidityType variableType) {
        assert scope != null;
        assert variableType != null;
        return this.selectScopeVariable(scope.variableStream(), this.getTypeEqualityMatcher(variableType));
    }

    private boolean matchAny(Variable variable) {
        return true;
    }

    private Predicate<Variable> getTypeEqualityMatcher(SolidityType type) {
        return variable -> variable.getType().conceptuallyEquals(type);
    }

    private Variable selectScopeVariable(Stream<Variable> stream, Predicate<Variable> variableSelection) {
        return GeneratorUtils.randomElement(stream.filter(variableSelection));
    }

    public Variable generateUniqueVariable(Scope scope) {
        assert scope != null;

        Variable variable = null;
        do {
            variable = this.generateVariable();
        } while (this.variableNotExistent(variable, scope));
        
        return variable;
    }

    private boolean variableNotExistent(Variable variable, Scope scope) {
        return scope.variableStream()
            .anyMatch(existingVar -> existingVar.getName().equals(variable.getName()));
    }

    public Variable generateVariable() {
        final SolidityType type = GeneratorUtils.TYPE_GENERATOR.generateType();
        return this.generateVariable(type);
    }

    public Variable generateVariable(SolidityType type) {
        return new Variable(type, this.generateVariableName(), VariableCategory.USER_DEFINED, null);
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

	public String serializeVariableReference(Variable variable) {
        assert variable != null;
        return variable.getCategory() == VariableCategory.LITERAL
            ? GeneratorUtils.LITERAL_GENERATOR.serializeLiteralValue(variable)
            : variable.getName();
    }
    
    public String serializeVariableDefinition(Variable variable) {
        assert variable != null && variable.getCategory() == VariableCategory.USER_DEFINED;
        return String.format(
            "%s %s", 
            GeneratorUtils.TYPE_GENERATOR.toBaseKeyword(variable.getType()),
            variable.getName()
        );
    }
}