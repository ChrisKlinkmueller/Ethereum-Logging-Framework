package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.GlobalScope;
import au.csiro.data61.aap.program.suppliers.MethodCall;
import au.csiro.data61.aap.program.Scope;
import au.csiro.data61.aap.program.Statement;
import au.csiro.data61.aap.program.suppliers.ValueSupplier;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.suppliers.UserVariable;
import au.csiro.data61.aap.program.suppliers.UserVariableReference;

/**
 * StatementGenerator
 */
public class StatementGenerator {    
    private final Random random;
    public StatementGenerator(Random random) {
        assert random != null;
        this.random = random;
    }

    public void generateAndAddStatement(Scope scope) {
        assert scope != null;

        if (this.random.nextInt(100) < 20) {
            final MethodCall call = GeneratorUtils.METHOD_CALL_GENERATOR.generateMethodCall(scope);
            if (call != null) {
                this.addStatementToScope(scope, new Statement(call));
                return;
            }
        }

        final Variable variable = this.generateVariable(scope);
        final ValueSupplier valueSource = this.generateValueSource(scope, variable);
        this.addStatementToScope(scope, new Statement(variable, valueSource));
    }

    private void addStatementToScope(Scope scope, Statement statement) {
        statement.setEnclosingScope(scope);
        scope.addInstruction(statement);
    }

    private ValueSupplier generateValueSource(Scope scope, Variable variable) {
        final int randomNumber = this.random.nextInt(100); 

        if (randomNumber < 40) {
            final MethodCall call = GeneratorUtils.METHOD_CALL_GENERATOR.generateMethodCall(variable.getType(), scope);
            if (call != null) {
                return call;
            }
        }

        if (randomNumber < 60) {
            final Variable existingVariable =  GeneratorUtils.VARIABLE_GENERATOR.selectAnyVariable(scope, variable.getType());
            if (existingVariable != null) {
                return existingVariable;
            }
        }

        return GeneratorUtils.LITERAL_GENERATOR.generateLiteral(variable.getType());
    }

    private Variable generateVariable(Scope scope) {
        if (this.random.nextInt(100) < 33) {
            final Stream<Variable> variables = scope.findVariablesWithinScope(variable -> true);
            final Variable variable = GeneratorUtils.randomElement(variables);

            if (variable != null) {
                return variable;
            }            
        }

        return GeneratorUtils.VARIABLE_GENERATOR.generateUniqueVariable(scope);
    }

    public String serializeStatement(Statement statement) {
        assert statement != null;

        final String valueSource = this.serializeValueSource(statement.getSource());
        if (!statement.hasVariable()) {
            return String.format("%s;", valueSource);
        }

        final String variable = statement.getVariable() instanceof UserVariableReference
            ? GeneratorUtils.VARIABLE_GENERATOR.serializeVariableReference((UserVariableReference)statement.getVariable())
            : GeneratorUtils.VARIABLE_GENERATOR.serializeVariableDefinition((UserVariable)statement.getVariable());
        return String.format("%s = %s;", variable, valueSource);
    }

    private String serializeValueSource(ValueSupplier source) {
        if (source instanceof MethodCall) {
            return GeneratorUtils.METHOD_CALL_GENERATOR.serializeMethodCall((MethodCall)source);
        }
        else if (source instanceof UserVariable) {
            final UserVariable variable = (UserVariable)source;
            return GeneratorUtils.VARIABLE_GENERATOR.serializeVariableReference(variable);
        }
        else {
            throw new UnsupportedOperationException(String.format("Class '%s' not supported.", source.getClass()));
        }
    }

    public static void main(String[] args) {
        GlobalScope scope = new GlobalScope();

        for (int i = 0; i < 100; i++) {
            GeneratorUtils.STATEMENT_GENERATOR.generateAndAddStatement(scope);
        }

        scope.instructionStream()
            .filter(instr -> instr instanceof Statement)
            .map(instr -> (Statement)instr)
            .map(GeneratorUtils.STATEMENT_GENERATOR::serializeStatement)
            .forEach(stmt -> System.out.println(stmt));
    }

}