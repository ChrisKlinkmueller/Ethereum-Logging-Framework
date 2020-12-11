package blf.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import blf.grammar.BcqlParser;
import blf.library.Library;
import blf.library.MethodSignature;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.Token;

/**
 * StatementAnalyzer
 */
public class ExpressionStatementAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceListener varAnalyzer;

    public ExpressionStatementAnalyzer(ErrorCollector errorCollector, VariableExistenceListener variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.varAnalyzer = variableAnalyzer;
    }

    @Override
    public void clear() {}

    @Override
    public void exitMethodStatement(BcqlParser.MethodStatementContext ctx) {
        this.verifyMethodInvocation(ctx.methodInvocation());
    }

    @Override
    public void exitVariableAssignmentStatement(BcqlParser.VariableAssignmentStatementContext ctx) {
        final String rightHandSideType = this.determineExpressionReturnType(ctx.statementExpression());
        final String leftHandSideType = this.varAnalyzer.getVariableType(ctx.variableName().getText());
        this.verifyTypeCompatibility(ctx.start, leftHandSideType, rightHandSideType);
    }

    @Override
    public void exitVariableDeclarationStatement(BcqlParser.VariableDeclarationStatementContext ctx) {
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

    private String determineExpressionReturnType(BcqlParser.StatementExpressionContext statementExpression) {
        if (statementExpression.valueExpression() != null) {
            return InterpreterUtils.determineType(statementExpression.valueExpression(), this.varAnalyzer);
        } else if (statementExpression.methodInvocation() != null) {
            return this.verifyMethodInvocation(statementExpression.methodInvocation());
        } else {
            throw new UnsupportedOperationException(String.format("The expression type '%s' is not known.", statementExpression.getText()));
        }
    }

    private String verifyMethodInvocation(BcqlParser.MethodInvocationContext ctx) {
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

    private List<String> determineParameterTypes(BcqlParser.MethodInvocationContext ctx) {
        final List<String> paramTypes = new ArrayList<>();
        for (BcqlParser.ValueExpressionContext paramCtx : ctx.valueExpression()) {
            final String type = InterpreterUtils.determineType(paramCtx, this.varAnalyzer);
            if (type == null) {
                return null;
            }
            paramTypes.add(type);
        }
        return paramTypes;
    }
}
