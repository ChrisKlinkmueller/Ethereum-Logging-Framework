package au.csiro.data61.aap.elf.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.library.Library;
import au.csiro.data61.aap.elf.library.MethodSignature;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.StatementExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableAssignmentStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableDeclarationStatementContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * StatementAnalyzer
 */
public class ExpressionStatementAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceAnalyzer varAnalyzer;

    public ExpressionStatementAnalyzer(EventCollector errorCollector, VariableExistenceAnalyzer variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.varAnalyzer = variableAnalyzer;
    }

    @Override
    public void clear() {}

    @Override
    public void exitMethodStatement(MethodStatementContext ctx) {
        this.verifyMethodInvocation(ctx.methodInvocation());
    }

    @Override
    public void exitVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        final String rightHandSideType = this.determineExpressionReturnType(ctx.statementExpression());
        final String leftHandSideType = this.varAnalyzer.getVariableType(ctx.variableName().getText());
        this.verifyTypeCompatibility(ctx.start, leftHandSideType, rightHandSideType);
    }

    @Override
    public void exitVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        final String rightHandSideType = this.determineExpressionReturnType(ctx.statementExpression());
        this.verifyTypeCompatibility(ctx.start, ctx.solType().getText(), rightHandSideType);
    }

    private void verifyTypeCompatibility(Token token, String leftHandSideType, String rightHandSideType) {
        if (rightHandSideType == null || leftHandSideType == null) {
            return;
        }

        if (!TypeUtils.areCompatible(leftHandSideType, rightHandSideType)) {
            this.addError(token, String.format("Cannot assign a %s value to a %s variable.", rightHandSideType, leftHandSideType));
        }
    }

    private String determineExpressionReturnType(StatementExpressionContext statementExpression) {
        if (statementExpression.valueExpression() != null) {
            return InterpreterUtils.determineType(statementExpression.valueExpression(), this.varAnalyzer);
        } else if (statementExpression.methodInvocation() != null) {
            return this.verifyMethodInvocation(statementExpression.methodInvocation());
        } else {
            throw new UnsupportedOperationException(String.format("The expression type '%s' is not known.", statementExpression.getText()));
        }
    }

    private String verifyMethodInvocation(MethodInvocationContext ctx) {
        final List<String> paramTypes = this.determineParameterTypes(ctx);
        if (paramTypes == null) {
            return null;
        }

        final MethodSignature signature = Library.INSTANCE.retrieveSignature(ctx.methodName.getText(), paramTypes);
        if (signature != null) {
            return signature.getReturnType();
        }

        this.addError(
            ctx.start,
            String.format(
                "Method '%s' with parameters '%s' unknown.",
                ctx.methodName.getText(),
                paramTypes.stream().collect(Collectors.joining(", "))
            )
        );
        return null;
    }

    private List<String> determineParameterTypes(MethodInvocationContext ctx) {
        final List<String> paramTypes = new ArrayList<>();
        for (ValueExpressionContext paramCtx : ctx.valueExpression()) {
            final String type = InterpreterUtils.determineType(paramCtx, this.varAnalyzer);
            if (type == null) {
                return null;
            }
            paramTypes.add(type);
        }
        return paramTypes;
    }
}
