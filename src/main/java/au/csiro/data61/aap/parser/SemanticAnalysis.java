package au.csiro.data61.aap.parser;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.DocumentContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryFilterContext;
import au.csiro.data61.aap.parser.XbelParser.ScopeContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractsFilterContext;
import au.csiro.data61.aap.parser.XbelParser.TransactionFilterContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;

/**
 * SemanticAnalysis
 */
class SemanticAnalysis extends XbelBaseListener {
    private final ErrorCollector errorCollector;    
    private final List<SemanticAnalyzer> analyzers;

    public SemanticAnalysis(ErrorCollector errorCollector) {
        assert errorCollector != null;
        this.errorCollector = errorCollector;
        this.analyzers = new LinkedList<>();

        final VariableAnalyzer collector = new VariableAnalyzer(errorCollector);
        this.analyzers.add(collector);
        this.analyzers.add(new FilterAnalyzer(this.errorCollector));        
    }

    public void analyze(ParseTree parseTree) {
        this.analyzers.forEach(SemanticAnalyzer::clear);

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
       this.analyzers.forEach(l -> l.enterBlockFilter(ctx));
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitBlockFilter(ctx));
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.analyzers.forEach(l -> l.enterTransactionFilter(ctx));
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitTransactionFilter(ctx));
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.analyzers.forEach(l -> l.enterLogEntryFilter(ctx));
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitLogEntryFilter(ctx));
    }

    @Override
    public void enterSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.analyzers.forEach(l -> l.enterSmartContractsFilter(ctx));
    }

    @Override
    public void exitSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.analyzers.forEach(l -> l.exitSmartContractsFilter(ctx));
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.analyzers.forEach(l -> l.exitScope(ctx));
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.analyzers.forEach(l -> l.enterDocument(ctx));
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.analyzers.forEach(l -> l.exitDocument(ctx));
    }

    @Override
    public void enterVariableDefinition(VariableDefinitionContext ctx) {
        this.analyzers.forEach(l -> enterVariableDefinition(ctx));
    }
}