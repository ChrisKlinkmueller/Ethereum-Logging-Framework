package au.csiro.data61.aap.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.library.DefaultVariables;
import au.csiro.data61.aap.spec.BlockScope;
import au.csiro.data61.aap.spec.GlobalScope;
import au.csiro.data61.aap.spec.LogEntryScope;
import au.csiro.data61.aap.spec.Scope;
import au.csiro.data61.aap.spec.SmartContractScope;
import au.csiro.data61.aap.spec.TransactionScope;
import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * VariableGenerator
 */
public class VariableGenerator {
    private static final String NAME_FIRST_LETTER = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NAME_LETTERS = NAME_FIRST_LETTER + "0123456789";
    private static final Map<Class<? extends Scope>, Supplier<Stream<Variable>>> DEFAULT_VARIABLES;

    static {
        DEFAULT_VARIABLES = new HashMap<>();
        DEFAULT_VARIABLES.put(BlockScope.class, DefaultVariables::defaultBlockVariableStream);
        DEFAULT_VARIABLES.put(GlobalScope.class, DefaultVariables::defaultGlobalVariableStream);
        DEFAULT_VARIABLES.put(LogEntryScope.class, DefaultVariables::defaultLogEntryVariableStream);
        DEFAULT_VARIABLES.put(SmartContractScope.class, DefaultVariables::defaultSmartContractVariableStream);
        DEFAULT_VARIABLES.put(TransactionScope.class, DefaultVariables::defaultTransactionVariableStream);
    }


    
    private final Random random;  
    public VariableGenerator(Random random) {
        assert random != null;
        this.random = random;
    }

    public Variable getBlockVariable(Class<? extends Scope> scopeType) {
        assert scopeType != null;
        return this.getBlockVariable(scopeType, variable -> true);
    }

    public Variable getBlockVariable(Class<? extends Scope> scopeType, SolidityType variableType) {
        assert scopeType != null;
        assert variableType != null;
        return this.getBlockVariable(scopeType, variable -> variable.getType().conceptuallyEquals(variableType));
    }

    private Variable getBlockVariable(Class<? extends Scope> scopeType, Predicate<Variable> variableSelection) {
        final List<Variable> variables = DEFAULT_VARIABLES
            .get(scopeType).get()
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
            GeneratorUtils.generateString(this.random, NAME_FIRST_LETTER, 1),
            GeneratorUtils.generateString(this.random, NAME_LETTERS, 1 + this.random.nextInt(10))
        );
    }
}