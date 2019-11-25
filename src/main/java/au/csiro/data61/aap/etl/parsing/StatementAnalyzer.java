package au.csiro.data61.aap.etl.parsing;

import au.csiro.data61.aap.etl.util.TypeUtils;
import au.csiro.data61.aap.etl.parsing.EthqlParser.StatementContext;

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

        final String varName = this.getVariable(ctx);
        if (!this.variableAnalyzer.isVariableDefined(varName)) {
            return;
        }

        final String varType = this.variableAnalyzer.getVariableType(varName);

        if (ctx.valueCreation().methodCall() != null || ctx.valueCreation().variableReference() != null) {
            final String sourceType = this.getValueType(ctx);
            if (sourceType != null && !TypeUtils.areCompatible(varType, sourceType)) {
                this.addError(
                    ctx.valueCreation().start, 
                    String.format("Cannot convert from '%s' to '%s'", sourceType, varType)
                );
            }
        }
        else if (ctx.valueCreation().literal() != null) {
            if (!AnalyzerUtils.isTypeCompatible(varType, ctx.valueCreation().literal())) {
                this.addError(
                    ctx.valueCreation().start, 
                    String.format("Cannot convert literal '%s' to '%s'", ctx.valueCreation().literal().getText(), varType)
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

    private String getValueType(StatementContext ctx) {
        if (ctx.valueCreation().variableReference() != null) {
            return this.variableAnalyzer.getVariableType(ctx.valueCreation().variableReference().variableName().getText());
        }        
        else if (ctx.valueCreation().methodCall() != null) {
            return this.methodCallAnalyzer.getCalledMethodType(ctx.valueCreation().methodCall());
        }
        else {
            return null;
        }
    }

    private String getVariable(StatementContext ctx) {
        return ctx.variable().variableReference() != null 
            ? ctx.variable().variableReference().getText()
            : ctx.variable().variableDefinition().variableName().getText();
    }
            
}