package au.csiro.data61.aap.elf.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.elf.parsing.EthqlListener;
import au.csiro.data61.aap.elf.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.AndExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BooleanArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BooleanExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BytesArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ComparatorsContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ComparisonExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.DocumentContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementCsvContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementEventContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementLogContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementTraceContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ExpressionStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.FilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.GenericFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.IntArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntrySignatureContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.NamedEmitVariableContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.NotExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.OrExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SkippableLogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SolTypeContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SolTypeRuleContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.StatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.StatementExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.StringArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableAssignmentStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableDeclarationStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.XesEmitVariableContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.XesTypesContext;

/**
 * CompositeEthqlListener
 */
public class CompositeEthqlListener<T extends EthqlListener> implements EthqlListener {
    private final List<T> analyzers;

    public CompositeEthqlListener() {
        this.analyzers = new LinkedList<>();
    }

    public void addListener(T listener) {
        assert listener != null;
        this.analyzers.add(listener);
    }

    public Stream<T> listenerStream() {
        return this.analyzers.stream();
    }

    private <S> void notifyListener(BiConsumer<EthqlListener, S> consumer, S object) {
        this.analyzers.forEach(l -> consumer.accept(l, object));
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        this.notifyListener(EthqlListener::visitTerminal, node);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        this.notifyListener(EthqlListener::visitErrorNode, node);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        this.notifyListener(EthqlListener::enterEveryRule, ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        this.notifyListener(EthqlListener::exitEveryRule, ctx);
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.notifyListener(EthqlListener::enterDocument, ctx);
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.notifyListener(EthqlListener::exitDocument, ctx);
    }

    @Override
    public void enterStatement(StatementContext ctx) {
        this.notifyListener(EthqlListener::enterStatement, ctx);
    }

    @Override
    public void exitStatement(StatementContext ctx) {
        this.notifyListener(EthqlListener::exitStatement, ctx);
    }

    @Override
    public void enterScope(ScopeContext ctx) {
        this.notifyListener(EthqlListener::enterScope, ctx);
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.notifyListener(EthqlListener::exitScope, ctx);
    }

    @Override
    public void enterFilter(FilterContext ctx) {
        this.notifyListener(EthqlListener::enterFilter, ctx);
    }

    @Override
    public void exitFilter(FilterContext ctx) {
        this.notifyListener(EthqlListener::exitFilter, ctx);
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.notifyListener(EthqlListener::enterBlockFilter, ctx);
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        this.notifyListener(EthqlListener::exitBlockFilter, ctx);
    }

    @Override
    public void enterBlockNumber(BlockNumberContext ctx) {
        this.notifyListener(EthqlListener::enterBlockNumber, ctx);
    }

    @Override
    public void exitBlockNumber(BlockNumberContext ctx) {
        this.notifyListener(EthqlListener::exitBlockNumber, ctx);
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.notifyListener(EthqlListener::enterTransactionFilter, ctx);
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.notifyListener(EthqlListener::exitTransactionFilter, ctx);
    }

    @Override
    public void enterAddressList(AddressListContext ctx) {
        this.notifyListener(EthqlListener::enterAddressList, ctx);
    }

    @Override
    public void exitAddressList(AddressListContext ctx) {
        this.notifyListener(EthqlListener::exitAddressList, ctx);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.notifyListener(EthqlListener::enterLogEntryFilter, ctx);
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.notifyListener(EthqlListener::exitLogEntryFilter, ctx);
    }

    @Override
    public void enterLogEntrySignature(LogEntrySignatureContext ctx) {
        this.notifyListener(EthqlListener::enterLogEntrySignature, ctx);
    }

    @Override
    public void exitLogEntrySignature(LogEntrySignatureContext ctx) {
        this.notifyListener(EthqlListener::exitLogEntrySignature, ctx);
    }

    @Override
    public void enterLogEntryParameter(LogEntryParameterContext ctx) {
        this.notifyListener(EthqlListener::enterLogEntryParameter, ctx);
    }

    @Override
    public void exitLogEntryParameter(LogEntryParameterContext ctx) {
        this.notifyListener(EthqlListener::exitLogEntryParameter, ctx);
    }

    @Override
    public void enterSkippableLogEntryParameter(SkippableLogEntryParameterContext ctx) {
        this.notifyListener(EthqlListener::enterSkippableLogEntryParameter, ctx);
    }

    @Override
    public void exitSkippableLogEntryParameter(SkippableLogEntryParameterContext ctx) {
        this.notifyListener(EthqlListener::exitSkippableLogEntryParameter, ctx);
    }

    @Override
    public void enterGenericFilter(GenericFilterContext ctx) {
        this.notifyListener(EthqlListener::enterGenericFilter, ctx);
    }

    @Override
    public void exitGenericFilter(GenericFilterContext ctx) {
        this.notifyListener(EthqlListener::exitGenericFilter, ctx);
    }

    @Override
    public void enterEmitStatement(EmitStatementContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatement, ctx);
    }

    @Override
    public void exitEmitStatement(EmitStatementContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatement, ctx);
    }

    @Override
    public void enterEmitStatementCsv(EmitStatementCsvContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatementCsv, ctx);
    }

    @Override
    public void exitEmitStatementCsv(EmitStatementCsvContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatementCsv, ctx);
    }

    @Override
    public void enterNamedEmitVariable(NamedEmitVariableContext ctx) {
        this.notifyListener(EthqlListener::enterNamedEmitVariable, ctx);
    }

    @Override
    public void exitNamedEmitVariable(NamedEmitVariableContext ctx) {
        this.notifyListener(EthqlListener::exitNamedEmitVariable, ctx);
    }

    @Override
    public void enterEmitStatementLog(EmitStatementLogContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatementLog, ctx);
    }

    @Override
    public void exitEmitStatementLog(EmitStatementLogContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatementLog, ctx);
    }

    @Override
    public void enterEmitStatementEvent(EmitStatementEventContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatementEvent, ctx);

    }

    @Override
    public void exitEmitStatementEvent(EmitStatementEventContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatementEvent, ctx);
    }

    @Override
    public void enterEmitStatementTrace(EmitStatementTraceContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatementTrace, ctx);

    }

    @Override
    public void exitEmitStatementTrace(EmitStatementTraceContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatementTrace, ctx);
    }

    @Override
    public void enterXesEmitVariable(XesEmitVariableContext ctx) {
        this.notifyListener(EthqlListener::enterXesEmitVariable, ctx);

    }

    @Override
    public void exitXesEmitVariable(XesEmitVariableContext ctx) {
        this.notifyListener(EthqlListener::exitXesEmitVariable, ctx);
    }

    @Override
    public void enterXesTypes(XesTypesContext ctx) {
        this.notifyListener(EthqlListener::enterXesTypes, ctx);

    }

    @Override
    public void exitXesTypes(XesTypesContext ctx) {
        this.notifyListener(EthqlListener::exitXesTypes, ctx);
    }

    @Override
    public void enterExpressionStatement(ExpressionStatementContext ctx) {
        this.notifyListener(EthqlListener::enterExpressionStatement, ctx);

    }

    @Override
    public void exitExpressionStatement(ExpressionStatementContext ctx) {
        this.notifyListener(EthqlListener::exitExpressionStatement, ctx);
    }

    @Override
    public void enterVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.notifyListener(EthqlListener::enterVariableDeclarationStatement, ctx);

    }

    @Override
    public void exitVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.notifyListener(EthqlListener::exitVariableDeclarationStatement, ctx);
    }

    @Override
    public void enterVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.notifyListener(EthqlListener::enterVariableAssignmentStatement, ctx);

    }

    @Override
    public void exitVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.notifyListener(EthqlListener::exitVariableAssignmentStatement, ctx);
    }

    @Override
    public void enterStatementExpression(StatementExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterStatementExpression, ctx);
    }

    @Override
    public void exitStatementExpression(StatementExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitStatementExpression, ctx);
    }

    @Override
    public void enterBooleanExpression(BooleanExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterBooleanExpression, ctx);
    }

    @Override
    public void exitBooleanExpression(BooleanExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitBooleanExpression, ctx);
    }

    @Override
    public void enterNotExpression(NotExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterNotExpression, ctx);

    }

    @Override
    public void exitNotExpression(NotExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitNotExpression, ctx);
    }

    @Override
    public void enterOrExpression(OrExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterOrExpression, ctx);
    }

    @Override
    public void exitOrExpression(OrExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitOrExpression, ctx);
    }

    @Override
    public void enterAndExpression(AndExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterAndExpression, ctx);
    }

    @Override
    public void exitAndExpression(AndExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitAndExpression, ctx);
    }

    @Override
    public void enterComparisonExpression(ComparisonExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterComparisonExpression, ctx);
    }

    @Override
    public void exitComparisonExpression(ComparisonExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitComparisonExpression, ctx);
    }

    @Override
    public void enterComparators(ComparatorsContext ctx) {
        this.notifyListener(EthqlListener::enterComparators, ctx);
    }

    @Override
    public void exitComparators(ComparatorsContext ctx) {
        this.notifyListener(EthqlListener::exitComparators, ctx);
    }

    @Override
    public void enterMethodInvocation(MethodInvocationContext ctx) {
        this.notifyListener(EthqlListener::enterMethodInvocation, ctx);
    }

    @Override
    public void exitMethodInvocation(MethodInvocationContext ctx) {
        this.notifyListener(EthqlListener::exitMethodInvocation, ctx);
    }

    @Override
    public void enterMethodParameter(MethodParameterContext ctx) {
        this.notifyListener(EthqlListener::enterMethodParameter, ctx);
    }

    @Override
    public void exitMethodParameter(MethodParameterContext ctx) {
        this.notifyListener(EthqlListener::exitMethodParameter, ctx);
    }

    @Override
    public void enterVariableName(VariableNameContext ctx) {
        this.notifyListener(EthqlListener::enterVariableName, ctx);
    }

    @Override
    public void exitVariableName(VariableNameContext ctx) {
        this.notifyListener(EthqlListener::exitVariableName, ctx);
    }

    @Override
    public void enterLiteral(LiteralContext ctx) {
        this.notifyListener(EthqlListener::enterLiteral, ctx);
    }

    @Override
    public void exitLiteral(LiteralContext ctx) {
        this.notifyListener(EthqlListener::exitLiteral, ctx);
    }

    @Override
    public void enterArrayLiteral(ArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::enterArrayLiteral, ctx);
    }

    @Override
    public void exitArrayLiteral(ArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::exitArrayLiteral, ctx);
    }

    @Override
    public void enterStringArrayLiteral(StringArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::enterStringArrayLiteral, ctx);
    }

    @Override
    public void exitStringArrayLiteral(StringArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::exitStringArrayLiteral, ctx);
    }

    @Override
    public void enterIntArrayLiteral(IntArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::enterIntArrayLiteral, ctx);
    }

    @Override
    public void exitIntArrayLiteral(IntArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::exitIntArrayLiteral, ctx);
    }

    @Override
    public void enterBooleanArrayLiteral(BooleanArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::enterBooleanArrayLiteral, ctx);
    }

    @Override
    public void exitBooleanArrayLiteral(BooleanArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::exitBooleanArrayLiteral, ctx);
    }

    @Override
    public void enterBytesArrayLiteral(BytesArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::enterBytesArrayLiteral, ctx);
    }

    @Override
    public void exitBytesArrayLiteral(BytesArrayLiteralContext ctx) {
        this.notifyListener(EthqlListener::exitBytesArrayLiteral, ctx);
    }

    @Override
    public void enterSolTypeRule(SolTypeRuleContext ctx) {
        this.notifyListener(EthqlListener::enterSolTypeRule, ctx);
    }

    @Override
    public void exitSolTypeRule(SolTypeRuleContext ctx) {
        this.notifyListener(EthqlListener::exitSolTypeRule, ctx);
    }

    @Override
    public void enterSolType(SolTypeContext ctx) {
        this.notifyListener(EthqlListener::enterSolType, ctx);
    }

    @Override
    public void exitSolType(SolTypeContext ctx) {
        this.notifyListener(EthqlListener::exitSolType, ctx);
    }

    @Override
    public void enterValueExpression(ValueExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterValueExpression, ctx);
    }

    @Override
    public void exitValueExpression(ValueExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitValueExpression, ctx);
    }
}