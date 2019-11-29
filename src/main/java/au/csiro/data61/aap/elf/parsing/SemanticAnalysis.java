package au.csiro.data61.aap.elf.parsing;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.util.CompositeEthqlListener;

/**
 * SemanticAnalysis
 */
class SemanticAnalysis extends CompositeEthqlListener<SemanticAnalyzer> {
    
    public SemanticAnalysis(ErrorCollector errorCollector) {
        assert errorCollector != null;
        final VariableExistenceAnalyzer varAnalyzer = new VariableExistenceAnalyzer(errorCollector);
        this.addListener(varAnalyzer);
        this.addListener(new FilterNestingAnalyzer(errorCollector));
        this.addListener(new FilterDefinitionAnalyzer(errorCollector, varAnalyzer));
        this.addListener(new EmitAnalyzer(errorCollector, varAnalyzer));
        this.addListener(new ExpressionStatementAnalyzer(errorCollector, varAnalyzer));
    }

    public void analyze(ParseTree parseTree) {
        this.listenerStream().forEach(SemanticAnalyzer::clear);

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
    }
    
}