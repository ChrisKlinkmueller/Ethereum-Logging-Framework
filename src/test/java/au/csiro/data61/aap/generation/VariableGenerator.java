package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.Scope;
import au.csiro.data61.aap.program.suppliers.Literal;
import au.csiro.data61.aap.program.suppliers.UserVariable;
import au.csiro.data61.aap.program.suppliers.ValueSupplier;
import au.csiro.data61.aap.program.suppliers.Variable;
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

    private Variable selectScopeVariable(Stream<? extends Variable> stream, Predicate<Variable> variableSelection) {
        return GeneratorUtils.randomElement(stream.filter(variableSelection));
    }

    public UserVariable generateUniqueVariable(Scope scope) {
        assert scope != null;

        UserVariable variable = null;
        do {
            variable = this.generateVariable();
        } while (this.variableNotExistent(variable, scope));
        
        return variable;
    }

    private boolean variableNotExistent(UserVariable variable, Scope scope) {
        return scope.variableStream()
            .anyMatch(existingVar -> existingVar.getName().equals(variable.getName()));
    }

    public UserVariable generateVariable() {
        final SolidityType type = GeneratorUtils.TYPE_GENERATOR.generateType();
        return this.generateVariable(type);
    }

    public UserVariable generateVariable(SolidityType type) {
        return new UserVariable(type, this.generateVariableName(), null);
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

	public String serializeVariableReference(ValueSupplier supplier) {
        assert supplier != null;
        if (supplier instanceof Literal) {
            return GeneratorUtils.LITERAL_GENERATOR.serializeLiteralValue((Literal)supplier);
        }
        else if (Variable.class.isAssignableFrom(supplier.getClass())) {
            return ((Variable)supplier).getName();
        }
        else {
            String msg = String.format("Class '%s' is not supported.", supplier.getClass());
            throw new IllegalArgumentException(msg);
        }
    }
    
    public String serializeVariableDefinition(UserVariable variable) {
        assert variable != null;
        return String.format(
            "%s %s", 
            GeneratorUtils.TYPE_GENERATOR.toBaseKeyword(variable.getType()),
            variable.getName()
        );
    }
}