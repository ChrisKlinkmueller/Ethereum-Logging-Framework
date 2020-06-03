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
import au.csiro.data61.aap.elf.parsing.EthqlParser.ArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BooleanArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BytesArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ComparatorsContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalAndExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalComparisonExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalNotExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalOrExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ConditionalPrimaryExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.DocumentContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementCsvContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementLogContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesEventContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.EmitStatementXesTraceContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ExpressionStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.FilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.GenericFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.IntArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntrySignatureContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.NamedEmitVariableContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.PublicFunctionQueryContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.PublicVariableQueryContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SkippableLogEntryParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractParameterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractQueryContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.SmartContractQueryParameterContext;
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

    @Override
    public void enterMethodStatement(MethodStatementContext ctx) {
        this.notifyListener(EthqlListener::enterMethodStatement, ctx);
    }

    @Override
    public void exitMethodStatement(MethodStatementContext ctx) {
        this.notifyListener(EthqlListener::exitMethodStatement, ctx);
    }

    @Override
    public void enterEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatementXesTrace, ctx);
    }

    @Override
    public void exitEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatementXesTrace, ctx);
    }

    @Override
    public void enterEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.notifyListener(EthqlListener::enterEmitStatementXesEvent, ctx);
    }

    @Override
    public void exitEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.notifyListener(EthqlListener::exitEmitStatementXesEvent, ctx);
    }

    @Override
    public void enterConditionalExpression(ConditionalExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterConditionalExpression, ctx);
    }

    @Override
    public void exitConditionalExpression(ConditionalExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitConditionalExpression, ctx);
    }

    @Override
    public void enterConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterConditionalOrExpression, ctx);
    }

    @Override
    public void exitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitConditionalOrExpression, ctx);
    }

    @Override
    public void enterConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterConditionalAndExpression, ctx);
    }

    @Override
    public void exitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitConditionalAndExpression, ctx);
    }

    @Override
    public void enterConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterConditionalComparisonExpression, ctx);
    }

    @Override
    public void exitConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitConditionalComparisonExpression, ctx);
    }

    @Override
    public void enterConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterConditionalNotExpression, ctx);
    }

    @Override
    public void exitConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitConditionalNotExpression, ctx);
    }

    @Override
    public void enterConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.notifyListener(EthqlListener::enterConditionalPrimaryExpression, ctx);
    }

    @Override
    public void exitConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.notifyListener(EthqlListener::exitConditionalPrimaryExpression, ctx);
    }

    @Override
    public void enterSmartContractFilter(SmartContractFilterContext ctx) {
        this.notifyListener(EthqlListener::enterSmartContractFilter, ctx);
    }

    @Override
    public void exitSmartContractFilter(SmartContractFilterContext ctx) {
        this.notifyListener(EthqlListener::exitSmartContractFilter, ctx);
    }

    @Override
    public void enterSmartContractQuery(SmartContractQueryContext ctx) {
        this.notifyListener(EthqlListener::enterSmartContractQuery, ctx);
    }

    @Override
    public void exitSmartContractQuery(SmartContractQueryContext ctx) {
        this.notifyListener(EthqlListener::exitSmartContractQuery, ctx);
    }

    @Override
    public void enterPublicVariableQuery(PublicVariableQueryContext ctx) {
        this.notifyListener(EthqlListener::enterPublicVariableQuery, ctx);
    }

    @Override
    public void exitPublicVariableQuery(PublicVariableQueryContext ctx) {
        this.notifyListener(EthqlListener::exitPublicVariableQuery, ctx);
    }

    @Override
    public void enterPublicFunctionQuery(PublicFunctionQueryContext ctx) {
        this.notifyListener(EthqlListener::enterPublicFunctionQuery, ctx);
    }

    @Override
    public void exitPublicFunctionQuery(PublicFunctionQueryContext ctx) {
        this.notifyListener(EthqlListener::exitPublicFunctionQuery, ctx);
    }

    @Override
    public void enterSmartContractQueryParameter(SmartContractQueryParameterContext ctx) {
        this.notifyListener(EthqlListener::enterSmartContractQueryParameter, ctx);
    }

    @Override
    public void exitSmartContractQueryParameter(SmartContractQueryParameterContext ctx) {
        this.notifyListener(EthqlListener::exitSmartContractQueryParameter, ctx);
    }

    @Override
    public void enterSmartContractParameter(SmartContractParameterContext ctx) {
        this.notifyListener(EthqlListener::enterSmartContractParameter, ctx);
    }

    @Override
    public void exitSmartContractParameter(SmartContractParameterContext ctx) {
        this.notifyListener(EthqlListener::exitSmartContractParameter, ctx);
    }
}
