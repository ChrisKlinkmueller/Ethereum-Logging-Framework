package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.elf.grammar.EthqlListener;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ArrayLiteralContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.BlockStatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.BooleanArrayLiteralContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.BytesArrayLiteralContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ComparatorsContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ConditionalAndExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ConditionalComparisonExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ConditionalExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ConditionalNotExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ConditionalOrExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ConditionalPrimaryExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.DocumentContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ExpressionStatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.IfScopeContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.IntArrayLiteralContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.MethodStatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.PluginStatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.ScopeDefinitionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.StatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.StatementExpressionContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.StringArrayLiteralContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.TypeContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.VariableAssignmentStatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.VariableDeclarationStatementContext;
import au.csiro.data61.aap.elf.grammar.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.library.Library;
import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

class Analyzer implements EthqlListener {
    private final InterpretationEventCollector eventCollector;
    private final List<EthqlListener> rules;
    private final Library library;

    Analyzer(Library library) {
        checkNotNull(library);
        this.library = library;

        this.rules = new LinkedList<>();

        this.eventCollector = new InterpretationEventCollector();

        final SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(this.eventCollector);
        this.rules.add(symbolTableBuilder);
        this.rules.add(new ConfigureInPreambleRule(this.eventCollector, symbolTableBuilder));

        this.rules.addAll(this.library.getAnalysisRules());
    }
    
    InterpretationResult<ParseTree> analyze(ParseTree parseTree) {
        return InterpretationResult.failure(new InterpretationEvent(Type.ERROR, "Method not implemented!"));
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        this.invokeRules(node, EthqlListener::visitTerminal);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        this.invokeRules(node, EthqlListener::visitErrorNode);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterEveryRule);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitEveryRule);
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterDocument);
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterEveryRule);
    }

    @Override
    public void enterStatement(StatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterStatement);
    }

    @Override
    public void exitStatement(StatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitStatement);
    }

    @Override
    public void enterBlockStatement(BlockStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterBlockStatement);
    }

    @Override
    public void exitBlockStatement(BlockStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitBlockStatement);
    }

    @Override
    public void enterScopeDefinition(ScopeDefinitionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterScopeDefinition);
    }

    @Override
    public void exitScopeDefinition(ScopeDefinitionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitScopeDefinition);
    }

    @Override
    public void enterIfScope(IfScopeContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterIfScope);
    }

    @Override
    public void exitIfScope(IfScopeContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitIfScope);
    }

    @Override
    public void enterExpressionStatement(ExpressionStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterExpressionStatement);
    }

    @Override
    public void exitExpressionStatement(ExpressionStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitExpressionStatement);
    }

    @Override
    public void enterPluginStatement(PluginStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterPluginStatement);
    }

    @Override
    public void exitPluginStatement(PluginStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitPluginStatement);
    }

    @Override
    public void enterMethodStatement(MethodStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterMethodStatement);
    }

    @Override
    public void exitMethodStatement(MethodStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitMethodStatement);
    }

    @Override
    public void enterVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterVariableDeclarationStatement);
    }

    @Override
    public void exitVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitVariableDeclarationStatement);
    }

    @Override
    public void enterVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterVariableAssignmentStatement);
    }

    @Override
    public void exitVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitVariableAssignmentStatement);
    }

    @Override
    public void enterStatementExpression(StatementExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterStatementExpression);
    }

    @Override
    public void exitStatementExpression(StatementExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitStatementExpression);
    }

    @Override
    public void enterExpression(ExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterExpression);
    }

    @Override
    public void exitExpression(ExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitExpression);
    }

    @Override
    public void enterMethodInvocation(MethodInvocationContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterMethodInvocation);
    }

    @Override
    public void exitMethodInvocation(MethodInvocationContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitMethodInvocation);
    }

    @Override
    public void enterVariableName(VariableNameContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterVariableName);
    }

    @Override
    public void exitVariableName(VariableNameContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitVariableName);
    }

    @Override
    public void enterConditionalExpression(ConditionalExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterConditionalExpression);
    }

    @Override
    public void exitConditionalExpression(ConditionalExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitConditionalExpression);
    }

    @Override
    public void enterConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterConditionalOrExpression);
    }

    @Override
    public void exitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitConditionalOrExpression);
    }

    @Override
    public void enterConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterConditionalAndExpression);
    }

    @Override
    public void exitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitConditionalAndExpression);
    }

    @Override
    public void enterConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterConditionalComparisonExpression);
    }

    @Override
    public void exitConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitConditionalComparisonExpression);
    }

    @Override
    public void enterConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterConditionalNotExpression);
    }

    @Override
    public void exitConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitConditionalNotExpression);
    }

    @Override
    public void enterConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterConditionalPrimaryExpression);
    }

    @Override
    public void exitConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitConditionalPrimaryExpression);
    }

    @Override
    public void enterComparators(ComparatorsContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterComparators);
    }

    @Override
    public void exitComparators(ComparatorsContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitComparators);
    }

    @Override
    public void enterType(TypeContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterType);
    }

    @Override
    public void exitType(TypeContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitType);
    }

    @Override
    public void enterLiteral(LiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterLiteral);
    }

    @Override
    public void exitLiteral(LiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitLiteral);
    }

    @Override
    public void enterArrayLiteral(ArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterArrayLiteral);
    }

    @Override
    public void exitArrayLiteral(ArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitArrayLiteral);
    }

    @Override
    public void enterStringArrayLiteral(StringArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterStringArrayLiteral);
    }

    @Override
    public void exitStringArrayLiteral(StringArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitStringArrayLiteral);
    }

    @Override
    public void enterIntArrayLiteral(IntArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterIntArrayLiteral);
    }

    @Override
    public void exitIntArrayLiteral(IntArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitIntArrayLiteral);
    }

    @Override
    public void enterBooleanArrayLiteral(BooleanArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterBooleanArrayLiteral);
    }

    @Override
    public void exitBooleanArrayLiteral(BooleanArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitBooleanArrayLiteral);
    }

    @Override
    public void enterBytesArrayLiteral(BytesArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::enterBytesArrayLiteral);
    }

    @Override
    public void exitBytesArrayLiteral(BytesArrayLiteralContext ctx) {
        this.invokeRules(ctx, EthqlListener::exitBytesArrayLiteral);
    }

    private <T extends ParseTree> void invokeRules(T ctx, BiConsumer<EthqlListener, T> rule) {
        this.rules.forEach(l -> rule.accept(l, ctx));
    }

}
