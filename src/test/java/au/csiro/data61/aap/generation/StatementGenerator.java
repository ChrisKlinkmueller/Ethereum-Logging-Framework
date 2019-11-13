package au.csiro.data61.aap.generation;

import java.util.Random;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.GlobalScope;
import au.csiro.data61.aap.program.Instruction;
import au.csiro.data61.aap.program.suppliers.MethodCall;
import au.csiro.data61.aap.program.Scope;
import au.csiro.data61.aap.program.Statement;
import au.csiro.data61.aap.program.suppliers.ValueSupplier;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.suppliers.VariableCategory;

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
            final Stream<Variable> variables = scope.findVariablesWithinScope(variable -> variable.getCategory() == VariableCategory.USER_DEFINED);
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
        if (statement.getVariable().isEmpty()) {
            return String.format("%s;", valueSource);
        }

        final String variable = this.isVariableAlreadyDefined(statement.getVariable().get(), statement, statement.getEnclosingScope())
            ? GeneratorUtils.VARIABLE_GENERATOR.serializeVariableReference(statement.getVariable().get())
            : GeneratorUtils.VARIABLE_GENERATOR.serializeVariableDefinition(statement.getVariable().get());
        return String.format("%s = %s;", variable, valueSource);
    }

    private boolean isVariableAlreadyDefined(Variable variable, Instruction currentInstruction, Scope scope) {
        if (scope.variableStream().anyMatch(scopeVariable -> scopeVariable.hasSameName(variable))) {
            return true;
        }

        for (int i = 0; i < scope.instructionCount(); i++) {
            Instruction instruction = scope.getInstruction(i);
            if (instruction == currentInstruction) {
                break;
            }
            if (   instruction instanceof Statement 
                && ((Statement)instruction).getVariable().isPresent()
                && ((Statement)instruction).getVariable().get().hasSameName(variable)
            ) {
                return true;
            }
        }

        return scope.getEnclosingScope() == null ? false : this.isVariableAlreadyDefined(variable, scope, scope.getEnclosingScope());
    }

    private String serializeValueSource(ValueSupplier source) {
        if (source instanceof MethodCall) {
            return GeneratorUtils.METHOD_CALL_GENERATOR.serializeMethodCall((MethodCall)source);
        }
        else if (source instanceof Variable) {
            final Variable variable = (Variable)source;
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