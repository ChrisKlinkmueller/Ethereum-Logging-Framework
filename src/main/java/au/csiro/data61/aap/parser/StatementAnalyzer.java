package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.parser.XbelParser.StatementContext;
import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityType;

/**
 * StatementAnalyzer
 */
public class StatementAnalyzer extends SemanticAnalyzer {
    private final VariableAnalyzer variableAnalyzer;
    private final MethodCallAnalyzer methodCallAnalyzer;

    public StatementAnalyzer(ErrorCollector errorCollector, VariableAnalyzer variableAnalyzer, MethodCallAnalyzer methodCallAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        assert methodCallAnalyzer != null;
        this.variableAnalyzer = variableAnalyzer;
        this.methodCallAnalyzer = methodCallAnalyzer;
    }

    @Override
    public void clear() {}

    @Override
    public void exitStatement(StatementContext ctx) {
        if (ctx.variable() == null) {
            if (ctx.valueCreation().variableReference() != null || ctx.valueCreation().literal() != null) {
                this.addError(
                    ctx.valueCreation().start, 
                    "A statement must either declare a variable and assign a value or call a method. Variable references or literals cannot be statements by themselves."
                );
            }            
            return;
        }

        final Variable variable = this.getVariable(ctx);
        if (variable == null) {
            return;
        }

        if (ctx.valueCreation().methodCall() != null || ctx.valueCreation().variableReference() != null) {
            final SolidityType sourceType = this.getValueType(ctx);
            if (sourceType != null && !variable.getType().conceptuallyEquals(sourceType)) {
                this.addError(
                    ctx.valueCreation().start, 
                    String.format("Cannot convert from '%s' to '%s'", sourceType, variable.getType())
                );
            }
        }
        else if (ctx.valueCreation().literal() != null) {
            if (!AnalyzerUtils.isTypeCompatible(variable.getType(), ctx.valueCreation().literal())) {
                this.addError(
                    ctx.valueCreation().start, 
                    String.format("Cannot convert literal '%s' to '%s'", ctx.valueCreation().literal().getText(), variable.getType())
                );
            }
        }
        else {
            throw new UnsupportedOperationException(
                String.format(
                    "This option of defining a value source in a statement is not supported: %s.",
                    ctx.valueCreation().getText()
                )
            );
        }
    }

    private SolidityType getValueType(StatementContext ctx) {
        if (ctx.valueCreation().variableReference() != null) {
            return this.variableAnalyzer.getVariableType(ctx.valueCreation().variableReference().variableName().getText());
        }        
        else if (ctx.valueCreation().methodCall() != null) {
            final Method method = this.methodCallAnalyzer.getCalledMethod(ctx.valueCreation().methodCall());
            return method == null ? null : method.getSignature().getReturnType();
        }
        else {
            return null;
        }
    }

    private Variable getVariable(StatementContext ctx) {
        final String varName = ctx.variable().variableReference() != null 
            ? ctx.variable().variableReference().getText()
            : ctx.variable().variableDefinition().variableName().getText();
        return this.variableAnalyzer.getVariable(varName);
    }
            
}