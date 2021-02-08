package blf.util;

import blf.configuration.BaseBlockchainListener;
import blf.core.exceptions.ExceptionHandler;
import blf.grammar.BcqlBaseListener;
import blf.grammar.BcqlListener;
import blf.grammar.BcqlParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Forwards callbacks from ParseTreeWalker to every added BcqlListener.
 *
 * @see BcqlListener
 * @see ParseTreeWalker
 */
public class RootListener implements BcqlListener {
    private final List<BcqlBaseListener> listeners;

    protected Map<String, BaseBlockchainListener> blockchainListeners;

    public BaseBlockchainListener blockchainListener;

    public RootListener() {
        this.listeners = new LinkedList<>();
    }

    public RootListener(Map<String, BaseBlockchainListener> blockchainListeners) {
        this.listeners = new LinkedList<>();
        this.blockchainListeners = blockchainListeners;
    }

    public void addListener(BcqlBaseListener listener) throws RootListenerException {
        if (listener == null) {
            throw new RootListenerException("Listener is null");
        }
        this.listeners.add(listener);
    }

    public List<BcqlBaseListener> getListeners() {
        return this.listeners;
    }

    protected <S> void notifyListener(BiConsumer<BcqlListener, S> consumer, S object) {
        this.listeners.forEach(l -> consumer.accept(l, object));
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        this.notifyListener(BcqlListener::visitTerminal, node);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        this.notifyListener(BcqlListener::visitErrorNode, node);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        this.notifyListener(BcqlListener::enterEveryRule, ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        this.notifyListener(BcqlListener::exitEveryRule, ctx);
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.notifyListener(BcqlListener::enterDocument, ctx);
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.notifyListener(BcqlListener::exitDocument, ctx);
    }

    @Override
    public void enterBlockchain(BlockchainContext ctx) {
        if (blockchainListeners == null) {
            return;
        }

        String blockchainKey = ctx.literal().STRING_LITERAL().getText().replace("\"", "").toLowerCase();

        BaseBlockchainListener targetBlockchainListener = blockchainListeners.get(blockchainKey);

        if (targetBlockchainListener == null) {
            final Logger logger = Logger.getLogger(RootListener.class.getName());
            logger.severe(String.format("The blockchain %s is not supported by the BLF!", blockchainKey));
            System.exit(1);
        }

        this.listeners.add(targetBlockchainListener);
        blockchainListener = targetBlockchainListener;

        this.notifyListener(BcqlListener::enterBlockchain, ctx);
    }

    @Override
    public void exitBlockchain(BlockchainContext ctx) {
        this.notifyListener(BcqlListener::exitBlockchain, ctx);
    }

    @Override
    public void enterOutputFolder(OutputFolderContext ctx) {
        String literalText = ListenerHelper.getOutputFolderLiteral(ctx);
        if (literalText == null) {
            return;
        }

        ExceptionHandler.getInstance().setOutputFolder(literalText);

        this.notifyListener(BcqlListener::enterOutputFolder, ctx);
    }

    @Override
    public void exitOutputFolder(OutputFolderContext ctx) {
        this.notifyListener(BcqlListener::exitOutputFolder, ctx);
    }

    @Override
    public void enterOptionalParams(OptionalParamsContext ctx) {
        this.notifyListener(BcqlListener::enterOptionalParams, ctx);
    }

    @Override
    public void exitOptionalParams(OptionalParamsContext ctx) {
        this.notifyListener(BcqlListener::exitOptionalParams, ctx);
    }

    @Override
    public void enterEmissionMode(EmissionModeContext ctx) {
        this.notifyListener(BcqlListener::enterEmissionMode, ctx);
    }

    @Override
    public void exitEmissionMode(EmissionModeContext ctx) {
        this.notifyListener(BcqlListener::exitEmissionMode, ctx);
    }

    @Override
    public void enterErrorOutput(ErrorOutputContext ctx) {
        this.notifyListener(BcqlListener::enterErrorOutput, ctx);
    }

    @Override
    public void enterAbortOnException(AbortOnExceptionContext ctx) {
        this.notifyListener(BcqlListener::enterAbortOnException, ctx);
    }

    @Override
    public void exitAbortOnException(AbortOnExceptionContext ctx) {
        this.notifyListener(BcqlListener::exitAbortOnException, ctx);
    }

    @Override
    public void exitErrorOutput(ErrorOutputContext ctx) {
        // we already know that we have at least one string literal, otherwise the grammar parser would have complained
        final String errorLogOutputFolderPathStringLiteral = ctx.STRING_LITERAL(0).getText();

        final String errorLogOutputFolderPathString = TypeUtils.parseStringLiteral(errorLogOutputFolderPathStringLiteral);

        ExceptionHandler.getInstance().setOutputFolder(errorLogOutputFolderPathString);

        if (ctx.STRING_LITERAL().size() < 2) {
            return;
        }

        final String errorLogFileNameStringLiteral = ctx.STRING_LITERAL(1).getText();

        final String errorLogFileName = TypeUtils.parseStringLiteral(errorLogFileNameStringLiteral);

        ExceptionHandler.getInstance().setOutputFilename(errorLogFileName);

        this.notifyListener(BcqlListener::exitErrorOutput, ctx);
    }

    @Override
    public void enterConnection(ConnectionContext ctx) {
        ExceptionHandler.getInstance().initializeLoggerHandler();
        this.notifyListener(BcqlListener::enterConnection, ctx);
    }

    @Override
    public void exitConnection(ConnectionContext ctx) {
        this.notifyListener(BcqlListener::exitConnection, ctx);
    }

    @Override
    public void enterStatement(StatementContext ctx) {
        this.notifyListener(BcqlListener::enterStatement, ctx);
    }

    @Override
    public void exitStatement(StatementContext ctx) {
        this.notifyListener(BcqlListener::exitStatement, ctx);
    }

    @Override
    public void enterScope(ScopeContext ctx) {
        this.notifyListener(BcqlListener::enterScope, ctx);
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.notifyListener(BcqlListener::exitScope, ctx);
    }

    @Override
    public void enterFilter(FilterContext ctx) {
        this.notifyListener(BcqlListener::enterFilter, ctx);
    }

    @Override
    public void exitFilter(FilterContext ctx) {
        this.notifyListener(BcqlListener::exitFilter, ctx);
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.notifyListener(BcqlListener::enterBlockFilter, ctx);
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        this.notifyListener(BcqlListener::exitBlockFilter, ctx);
    }

    @Override
    public void enterBlockNumber(BlockNumberContext ctx) {
        this.notifyListener(BcqlListener::enterBlockNumber, ctx);
    }

    @Override
    public void exitBlockNumber(BlockNumberContext ctx) {
        this.notifyListener(BcqlListener::exitBlockNumber, ctx);
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.notifyListener(BcqlListener::enterTransactionFilter, ctx);
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.notifyListener(BcqlListener::exitTransactionFilter, ctx);
    }

    @Override
    public void enterAddressList(AddressListContext ctx) {
        this.notifyListener(BcqlListener::enterAddressList, ctx);
    }

    @Override
    public void exitAddressList(AddressListContext ctx) {
        this.notifyListener(BcqlListener::exitAddressList, ctx);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.notifyListener(BcqlListener::enterLogEntryFilter, ctx);
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.notifyListener(BcqlListener::exitLogEntryFilter, ctx);
    }

    @Override
    public void enterLogEntrySignature(LogEntrySignatureContext ctx) {
        this.notifyListener(BcqlListener::enterLogEntrySignature, ctx);
    }

    @Override
    public void exitLogEntrySignature(LogEntrySignatureContext ctx) {
        this.notifyListener(BcqlListener::exitLogEntrySignature, ctx);
    }

    @Override
    public void enterLogEntryParameter(LogEntryParameterContext ctx) {
        this.notifyListener(BcqlListener::enterLogEntryParameter, ctx);
    }

    @Override
    public void exitLogEntryParameter(LogEntryParameterContext ctx) {
        this.notifyListener(BcqlListener::exitLogEntryParameter, ctx);
    }

    @Override
    public void enterSkippableLogEntryParameter(SkippableLogEntryParameterContext ctx) {
        this.notifyListener(BcqlListener::enterSkippableLogEntryParameter, ctx);
    }

    @Override
    public void exitSkippableLogEntryParameter(SkippableLogEntryParameterContext ctx) {
        this.notifyListener(BcqlListener::exitSkippableLogEntryParameter, ctx);
    }

    @Override
    public void enterGenericFilter(GenericFilterContext ctx) {
        this.notifyListener(BcqlListener::enterGenericFilter, ctx);
    }

    @Override
    public void exitGenericFilter(GenericFilterContext ctx) {
        this.notifyListener(BcqlListener::exitGenericFilter, ctx);
    }

    @Override
    public void enterEmitStatement(EmitStatementContext ctx) {
        this.notifyListener(BcqlListener::enterEmitStatement, ctx);
    }

    @Override
    public void exitEmitStatement(EmitStatementContext ctx) {
        this.notifyListener(BcqlListener::exitEmitStatement, ctx);
    }

    @Override
    public void enterEmitStatementCsv(EmitStatementCsvContext ctx) {
        this.notifyListener(BcqlListener::enterEmitStatementCsv, ctx);
    }

    @Override
    public void exitEmitStatementCsv(EmitStatementCsvContext ctx) {
        this.notifyListener(BcqlListener::exitEmitStatementCsv, ctx);
    }

    @Override
    public void enterNamedEmitVariable(NamedEmitVariableContext ctx) {
        this.notifyListener(BcqlListener::enterNamedEmitVariable, ctx);
    }

    @Override
    public void exitNamedEmitVariable(NamedEmitVariableContext ctx) {
        this.notifyListener(BcqlListener::exitNamedEmitVariable, ctx);
    }

    @Override
    public void enterEmitStatementLog(EmitStatementLogContext ctx) {
        this.notifyListener(BcqlListener::enterEmitStatementLog, ctx);
    }

    @Override
    public void exitEmitStatementLog(EmitStatementLogContext ctx) {
        this.notifyListener(BcqlListener::exitEmitStatementLog, ctx);
    }

    @Override
    public void enterXesEmitVariable(XesEmitVariableContext ctx) {
        this.notifyListener(BcqlListener::enterXesEmitVariable, ctx);

    }

    @Override
    public void exitXesEmitVariable(XesEmitVariableContext ctx) {
        this.notifyListener(BcqlListener::exitXesEmitVariable, ctx);
    }

    @Override
    public void enterXesTypes(XesTypesContext ctx) {
        this.notifyListener(BcqlListener::enterXesTypes, ctx);

    }

    @Override
    public void exitXesTypes(XesTypesContext ctx) {
        this.notifyListener(BcqlListener::exitXesTypes, ctx);
    }

    @Override
    public void enterExpressionStatement(ExpressionStatementContext ctx) {
        this.notifyListener(BcqlListener::enterExpressionStatement, ctx);

    }

    @Override
    public void exitExpressionStatement(ExpressionStatementContext ctx) {
        this.notifyListener(BcqlListener::exitExpressionStatement, ctx);
    }

    @Override
    public void enterVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.notifyListener(BcqlListener::enterVariableDeclarationStatement, ctx);

    }

    @Override
    public void exitVariableDeclarationStatement(VariableDeclarationStatementContext ctx) {
        this.notifyListener(BcqlListener::exitVariableDeclarationStatement, ctx);
    }

    @Override
    public void enterVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.notifyListener(BcqlListener::enterVariableAssignmentStatement, ctx);

    }

    @Override
    public void exitVariableAssignmentStatement(VariableAssignmentStatementContext ctx) {
        this.notifyListener(BcqlListener::exitVariableAssignmentStatement, ctx);
    }

    @Override
    public void enterStatementExpression(StatementExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterStatementExpression, ctx);
    }

    @Override
    public void exitStatementExpression(StatementExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitStatementExpression, ctx);
    }

    @Override
    public void enterComparators(ComparatorsContext ctx) {
        this.notifyListener(BcqlListener::enterComparators, ctx);
    }

    @Override
    public void exitComparators(ComparatorsContext ctx) {
        this.notifyListener(BcqlListener::exitComparators, ctx);
    }

    @Override
    public void enterMethodInvocation(MethodInvocationContext ctx) {
        this.notifyListener(BcqlListener::enterMethodInvocation, ctx);
    }

    @Override
    public void exitMethodInvocation(MethodInvocationContext ctx) {
        this.notifyListener(BcqlListener::exitMethodInvocation, ctx);
    }

    @Override
    public void enterVariableName(VariableNameContext ctx) {
        this.notifyListener(BcqlListener::enterVariableName, ctx);
    }

    @Override
    public void exitVariableName(VariableNameContext ctx) {
        this.notifyListener(BcqlListener::exitVariableName, ctx);
    }

    @Override
    public void enterLiteral(LiteralContext ctx) {
        this.notifyListener(BcqlListener::enterLiteral, ctx);
    }

    @Override
    public void exitLiteral(LiteralContext ctx) {
        this.notifyListener(BcqlListener::exitLiteral, ctx);
    }

    @Override
    public void enterArrayLiteral(ArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::enterArrayLiteral, ctx);
    }

    @Override
    public void exitArrayLiteral(ArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::exitArrayLiteral, ctx);
    }

    @Override
    public void enterStringArrayLiteral(StringArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::enterStringArrayLiteral, ctx);
    }

    @Override
    public void exitStringArrayLiteral(StringArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::exitStringArrayLiteral, ctx);
    }

    @Override
    public void enterIntArrayLiteral(IntArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::enterIntArrayLiteral, ctx);
    }

    @Override
    public void exitIntArrayLiteral(IntArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::exitIntArrayLiteral, ctx);
    }

    @Override
    public void enterBooleanArrayLiteral(BooleanArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::enterBooleanArrayLiteral, ctx);
    }

    @Override
    public void exitBooleanArrayLiteral(BooleanArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::exitBooleanArrayLiteral, ctx);
    }

    @Override
    public void enterBytesArrayLiteral(BytesArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::enterBytesArrayLiteral, ctx);
    }

    @Override
    public void exitBytesArrayLiteral(BytesArrayLiteralContext ctx) {
        this.notifyListener(BcqlListener::exitBytesArrayLiteral, ctx);
    }

    @Override
    public void enterSolTypeRule(SolTypeRuleContext ctx) {
        this.notifyListener(BcqlListener::enterSolTypeRule, ctx);
    }

    @Override
    public void exitSolTypeRule(SolTypeRuleContext ctx) {
        this.notifyListener(BcqlListener::exitSolTypeRule, ctx);
    }

    @Override
    public void enterSolType(SolTypeContext ctx) {
        this.notifyListener(BcqlListener::enterSolType, ctx);
    }

    @Override
    public void exitSolType(SolTypeContext ctx) {
        this.notifyListener(BcqlListener::exitSolType, ctx);
    }

    @Override
    public void enterValueExpression(ValueExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterValueExpression, ctx);
    }

    @Override
    public void exitValueExpression(ValueExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitValueExpression, ctx);
    }

    @Override
    public void enterMethodStatement(MethodStatementContext ctx) {
        this.notifyListener(BcqlListener::enterMethodStatement, ctx);
    }

    @Override
    public void exitMethodStatement(MethodStatementContext ctx) {
        this.notifyListener(BcqlListener::exitMethodStatement, ctx);
    }

    @Override
    public void enterEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.notifyListener(BcqlListener::enterEmitStatementXesTrace, ctx);
    }

    @Override
    public void exitEmitStatementXesTrace(EmitStatementXesTraceContext ctx) {
        this.notifyListener(BcqlListener::exitEmitStatementXesTrace, ctx);
    }

    @Override
    public void enterEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.notifyListener(BcqlListener::enterEmitStatementXesEvent, ctx);
    }

    @Override
    public void exitEmitStatementXesEvent(EmitStatementXesEventContext ctx) {
        this.notifyListener(BcqlListener::exitEmitStatementXesEvent, ctx);
    }

    @Override
    public void enterConditionalExpression(ConditionalExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterConditionalExpression, ctx);
    }

    @Override
    public void exitConditionalExpression(ConditionalExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitConditionalExpression, ctx);
    }

    @Override
    public void enterConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterConditionalOrExpression, ctx);
    }

    @Override
    public void exitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitConditionalOrExpression, ctx);
    }

    @Override
    public void enterConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterConditionalAndExpression, ctx);
    }

    @Override
    public void exitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitConditionalAndExpression, ctx);
    }

    @Override
    public void enterConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterConditionalComparisonExpression, ctx);
    }

    @Override
    public void exitConditionalComparisonExpression(ConditionalComparisonExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitConditionalComparisonExpression, ctx);
    }

    @Override
    public void enterConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterConditionalNotExpression, ctx);
    }

    @Override
    public void exitConditionalNotExpression(ConditionalNotExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitConditionalNotExpression, ctx);
    }

    @Override
    public void enterConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.notifyListener(BcqlListener::enterConditionalPrimaryExpression, ctx);
    }

    @Override
    public void exitConditionalPrimaryExpression(ConditionalPrimaryExpressionContext ctx) {
        this.notifyListener(BcqlListener::exitConditionalPrimaryExpression, ctx);
    }

    @Override
    public void enterSmartContractFilter(SmartContractFilterContext ctx) {
        this.notifyListener(BcqlListener::enterSmartContractFilter, ctx);
    }

    @Override
    public void exitSmartContractFilter(SmartContractFilterContext ctx) {
        this.notifyListener(BcqlListener::exitSmartContractFilter, ctx);
    }

    @Override
    public void enterSmartContractQuery(SmartContractQueryContext ctx) {
        this.notifyListener(BcqlListener::enterSmartContractQuery, ctx);
    }

    @Override
    public void exitSmartContractQuery(SmartContractQueryContext ctx) {
        this.notifyListener(BcqlListener::exitSmartContractQuery, ctx);
    }

    @Override
    public void enterPublicVariableQuery(PublicVariableQueryContext ctx) {
        this.notifyListener(BcqlListener::enterPublicVariableQuery, ctx);
    }

    @Override
    public void exitPublicVariableQuery(PublicVariableQueryContext ctx) {
        this.notifyListener(BcqlListener::exitPublicVariableQuery, ctx);
    }

    @Override
    public void enterPublicFunctionQuery(PublicFunctionQueryContext ctx) {
        this.notifyListener(BcqlListener::enterPublicFunctionQuery, ctx);
    }

    @Override
    public void exitPublicFunctionQuery(PublicFunctionQueryContext ctx) {
        this.notifyListener(BcqlListener::exitPublicFunctionQuery, ctx);
    }

    @Override
    public void enterSmartContractQueryParameter(SmartContractQueryParameterContext ctx) {
        this.notifyListener(BcqlListener::enterSmartContractQueryParameter, ctx);
    }

    @Override
    public void exitSmartContractQueryParameter(SmartContractQueryParameterContext ctx) {
        this.notifyListener(BcqlListener::exitSmartContractQueryParameter, ctx);
    }

    @Override
    public void enterSmartContractParameter(SmartContractParameterContext ctx) {
        this.notifyListener(BcqlListener::enterSmartContractParameter, ctx);
    }

    @Override
    public void exitSmartContractParameter(SmartContractParameterContext ctx) {
        this.notifyListener(BcqlListener::exitSmartContractParameter, ctx);
    }
}
