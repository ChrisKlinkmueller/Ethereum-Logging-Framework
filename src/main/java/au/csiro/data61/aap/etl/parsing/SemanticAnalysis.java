package au.csiro.data61.aap.etl.parsing;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.etl.parsing.EthqlBaseListener;
import au.csiro.data61.aap.etl.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.BooleanArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ByteAndAddressArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.DocumentContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.FilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.FixedArrayElementContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.FixedArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.InstructionContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.IntArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LiteralContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LiteralRuleContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LogEntrySignatureContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.MethodCallContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.MethodParameterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SkippableLogEntryParameterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SkippableLogEntrySignatureContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SmartContractSignatureContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SmartContractVariableContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SmartContractsFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SolTypeContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SolTypeRuleContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.StatementContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.StringArrayValueContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ValueCreationContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.VariableContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.VariableDefinitionContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.VariableDefinitionRuleContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.VariableReferenceContext;

/**
 * SemanticAnalysis
 */
class SemanticAnalysis extends EthqlBaseListener {
    private final ErrorCollector errorCollector;    
    private final List<SemanticAnalyzer> analyzers;

    public SemanticAnalysis(ErrorCollector errorCollector) {
        assert errorCollector != null;
        this.errorCollector = errorCollector;
        this.analyzers = new LinkedList<>();

        final VariableAnalyzer varAnalyzer = new VariableAnalyzer(errorCollector);
        this.analyzers.add(varAnalyzer);
        this.analyzers.add(new ScopeAnalyzer(this.errorCollector, varAnalyzer));    
        this.analyzers.add(new MethodCallAnalyzer(this.errorCollector, varAnalyzer));
    }

    public void analyze(ParseTree parseTree) {
        this.analyzers.forEach(SemanticAnalyzer::clear);

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
    }

    @Override
    public void enterAddressList(AddressListContext ctx) {
        this.analyzers.forEach(l -> l.enterAddressList(ctx));
    }

    @Override
    public void enterArrayValue(ArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.enterArrayValue(ctx));
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
       this.analyzers.forEach(l -> l.enterBlockFilter(ctx));
    }

    @Override
    public void enterBlockNumber(BlockNumberContext ctx) {
        this.analyzers.forEach(l -> l.enterBlockNumber(ctx));
    }

    @Override
    public void enterBooleanArrayValue(BooleanArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.enterBooleanArrayValue(ctx));
    }

    @Override
    public void enterByteAndAddressArrayValue(ByteAndAddressArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.enterByteAndAddressArrayValue(ctx));
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.analyzers.forEach(l -> l.enterDocument(ctx));
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        this.analyzers.forEach(l -> l.enterEveryRule(ctx));
    }

    @Override
    public void enterFilter(FilterContext ctx) {
        this.analyzers.forEach(l -> l.enterFilter(ctx));
    }

    @Override
    public void enterFixedArrayElement(FixedArrayElementContext ctx) {
        this.analyzers.forEach(l -> l.enterFixedArrayElement(ctx));
    }

    @Override
    public void enterFixedArrayValue(FixedArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.enterFixedArrayValue(ctx));
    }

    @Override
    public void enterInstruction(InstructionContext ctx) {
        this.analyzers.forEach(l -> l.enterInstruction(ctx));
    }

    @Override
    public void enterIntArrayValue(IntArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.enterIntArrayValue(ctx));
    }

    @Override
    public void enterLiteral(LiteralContext ctx) {
        this.analyzers.forEach(l -> l.enterLiteral(ctx));
    }

    @Override
    public void enterLiteralRule(LiteralRuleContext ctx) {
        this.analyzers.forEach(l -> l.enterLiteralRule(ctx));
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.analyzers.forEach(l -> l.enterLogEntryFilter(ctx));
    }

    @Override
    public void enterLogEntryParameter(LogEntryParameterContext ctx) {
        this.analyzers.forEach(l -> l.enterLogEntryParameter(ctx));
    }

    @Override
    public void enterLogEntrySignature(LogEntrySignatureContext ctx) {
        this.analyzers.forEach(l -> l.enterLogEntrySignature(ctx));
    }

    @Override
    public void enterMethodCall(MethodCallContext ctx) {
        this.analyzers.forEach(l -> l.enterMethodCall(ctx));
    }

    @Override
    public void enterMethodParameter(MethodParameterContext ctx) {
        this.analyzers.forEach(l -> l.enterMethodParameter(ctx));
    }

    @Override
    public void enterScope(ScopeContext ctx) {
        this.analyzers.forEach(l -> l.enterScope(ctx));
    }

    @Override
    public void enterSkippableLogEntryParameter(SkippableLogEntryParameterContext ctx) {
        this.analyzers.forEach(l -> l.enterSkippableLogEntryParameter(ctx));
    }

    @Override
    public void enterSkippableLogEntrySignature(SkippableLogEntrySignatureContext ctx) {
        this.analyzers.forEach(l -> l.enterSkippableLogEntrySignature(ctx));
    }

    @Override
    public void enterSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.analyzers.forEach(l -> l.enterSmartContractsFilter(ctx));
    }

    @Override
    public void enterSmartContractSignature(SmartContractSignatureContext ctx) {
        this.analyzers.forEach(l -> l.enterSmartContractSignature(ctx));
    }

    @Override
    public void enterSmartContractVariable(SmartContractVariableContext ctx) {
        this.analyzers.forEach(l -> l.enterSmartContractVariable(ctx));
    }

    @Override
    public void enterSolType(SolTypeContext ctx) {
        this.analyzers.forEach(l -> l.enterSolType(ctx));
    }

    @Override
    public void enterSolTypeRule(SolTypeRuleContext ctx) {
        this.analyzers.forEach(l -> l.enterSolTypeRule(ctx));
    }

    @Override
    public void enterStatement(StatementContext ctx) {
        this.analyzers.forEach(l -> l.enterStatement(ctx));
    }

    @Override
    public void enterStringArrayValue(StringArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.enterStringArrayValue(ctx));
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.analyzers.forEach(l -> l.enterTransactionFilter(ctx));
    }

    @Override
    public void enterValueCreation(ValueCreationContext ctx) {
        this.analyzers.forEach(l -> l.enterValueCreation(ctx));
    }

    @Override
    public void enterVariable(VariableContext ctx) {
        this.analyzers.forEach(l -> l.enterVariable(ctx));
    }

    @Override
    public void enterVariableDefinition(VariableDefinitionContext ctx) {
        this.analyzers.forEach(l -> l.enterVariableDefinition(ctx));
    }

    @Override
    public void enterVariableDefinitionRule(VariableDefinitionRuleContext ctx) {
        this.analyzers.forEach(l -> l.enterVariableDefinitionRule(ctx));
    }

    @Override
    public void enterVariableName(VariableNameContext ctx) {
        this.analyzers.forEach(l -> l.enterVariableName(ctx));
    }

    @Override
    public void enterVariableReference(VariableReferenceContext ctx) {
        this.analyzers.forEach(l -> l.enterVariableReference(ctx));
    }



    

    @Override
    public void exitAddressList(AddressListContext ctx) {
        this.analyzers.forEach(l -> l.exitAddressList(ctx));
    }

    @Override
    public void exitArrayValue(ArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.exitArrayValue(ctx));
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
       this.analyzers.forEach(l -> l.exitBlockFilter(ctx));
    }

    @Override
    public void exitBlockNumber(BlockNumberContext ctx) {
        this.analyzers.forEach(l -> l.exitBlockNumber(ctx));
    }

    @Override
    public void exitBooleanArrayValue(BooleanArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.exitBooleanArrayValue(ctx));
    }

    @Override
    public void exitByteAndAddressArrayValue(ByteAndAddressArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.exitByteAndAddressArrayValue(ctx));
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.analyzers.forEach(l -> l.exitDocument(ctx));
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        this.analyzers.forEach(l -> l.exitEveryRule(ctx));
    }

    @Override
    public void exitFilter(FilterContext ctx) {
        this.analyzers.forEach(l -> l.exitFilter(ctx));
    }

    @Override
    public void exitFixedArrayElement(FixedArrayElementContext ctx) {
        this.analyzers.forEach(l -> l.exitFixedArrayElement(ctx));
    }

    @Override
    public void exitFixedArrayValue(FixedArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.exitFixedArrayValue(ctx));
    }

    @Override
    public void exitInstruction(InstructionContext ctx) {
        this.analyzers.forEach(l -> l.exitInstruction(ctx));
    }

    @Override
    public void exitIntArrayValue(IntArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.exitIntArrayValue(ctx));
    }

    @Override
    public void exitLiteral(LiteralContext ctx) {
        this.analyzers.forEach(l -> l.exitLiteral(ctx));
    }

    @Override
    public void exitLiteralRule(LiteralRuleContext ctx) {
        this.analyzers.forEach(l -> l.exitLiteralRule(ctx));
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitLogEntryFilter(ctx));
    }

    @Override
    public void exitLogEntryParameter(LogEntryParameterContext ctx) {
        this.analyzers.forEach(l -> l.exitLogEntryParameter(ctx));
    }

    @Override
    public void exitLogEntrySignature(LogEntrySignatureContext ctx) {
        this.analyzers.forEach(l -> l.exitLogEntrySignature(ctx));
    }

    @Override
    public void exitMethodCall(MethodCallContext ctx) {
        this.analyzers.forEach(l -> l.exitMethodCall(ctx));
    }

    @Override
    public void exitMethodParameter(MethodParameterContext ctx) {
        this.analyzers.forEach(l -> l.exitMethodParameter(ctx));
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.analyzers.forEach(l -> l.exitScope(ctx));
    }

    @Override
    public void exitSkippableLogEntryParameter(SkippableLogEntryParameterContext ctx) {
        this.analyzers.forEach(l -> l.exitSkippableLogEntryParameter(ctx));
    }

    @Override
    public void exitSkippableLogEntrySignature(SkippableLogEntrySignatureContext ctx) {
        this.analyzers.forEach(l -> l.exitSkippableLogEntrySignature(ctx));
    }

    @Override
    public void exitSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitSmartContractsFilter(ctx));
    }

    @Override
    public void exitSmartContractSignature(SmartContractSignatureContext ctx) {
        this.analyzers.forEach(l -> l.exitSmartContractSignature(ctx));
    }

    @Override
    public void exitSmartContractVariable(SmartContractVariableContext ctx) {
        this.analyzers.forEach(l -> l.exitSmartContractVariable(ctx));
    }

    @Override
    public void exitSolType(SolTypeContext ctx) {
        this.analyzers.forEach(l -> l.exitSolType(ctx));
    }

    @Override
    public void exitSolTypeRule(SolTypeRuleContext ctx) {
        this.analyzers.forEach(l -> l.exitSolTypeRule(ctx));
    }

    @Override
    public void exitStatement(StatementContext ctx) {
        this.analyzers.forEach(l -> l.exitStatement(ctx));
    }

    @Override
    public void exitStringArrayValue(StringArrayValueContext ctx) {
        this.analyzers.forEach(l -> l.exitStringArrayValue(ctx));
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitTransactionFilter(ctx));
    }

    @Override
    public void exitValueCreation(ValueCreationContext ctx) {
        this.analyzers.forEach(l -> l.exitValueCreation(ctx));
    }

    @Override
    public void exitVariable(VariableContext ctx) {
        this.analyzers.forEach(l -> l.exitVariable(ctx));
    }

    @Override
    public void exitVariableDefinition(VariableDefinitionContext ctx) {
        this.analyzers.forEach(l -> l.exitVariableDefinition(ctx));
    }

    @Override
    public void exitVariableDefinitionRule(VariableDefinitionRuleContext ctx) {
        this.analyzers.forEach(l -> l.exitVariableDefinitionRule(ctx));
    }

    @Override
    public void exitVariableName(VariableNameContext ctx) {
        this.analyzers.forEach(l -> l.exitVariableName(ctx));
    }

    @Override
    public void exitVariableReference(VariableReferenceContext ctx) {
        this.analyzers.forEach(l -> l.exitVariableReference(ctx));
    }
    
}