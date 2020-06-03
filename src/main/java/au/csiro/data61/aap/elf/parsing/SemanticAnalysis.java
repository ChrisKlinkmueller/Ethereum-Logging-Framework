package au.csiro.data61.aap.elf.parsing;

import java.util.List;
import java.util.Objects;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.util.CompositeEthqlListener;

/**
 * SemanticAnalysis
 */
public class SemanticAnalysis extends CompositeEthqlListener<SemanticAnalyzer> {

    public SemanticAnalysis(ErrorCollector errorCollector) {
        assert errorCollector != null;
        final VariableExistenceAnalyzer varAnalyzer = new VariableExistenceAnalyzer(errorCollector);
        this.addListener(new FilterNestingAnalyzer(errorCollector));
        this.addListener(new FilterDefinitionAnalyzer(errorCollector, varAnalyzer));
        this.addListener(new EmitAnalyzer(errorCollector, varAnalyzer));
        this.addListener(new ExpressionStatementAnalyzer(errorCollector, varAnalyzer));
        this.addListener(varAnalyzer);
    }

    public SemanticAnalysis(List<SemanticAnalyzer> analyzers) {
        assert analyzers != null && analyzers.stream().allMatch(Objects::nonNull);
        analyzers.forEach(analyzer -> this.addListener(analyzer));
    }

    public void analyze(ParseTree parseTree) {
        this.listenerStream().forEach(SemanticAnalyzer::clear);

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
    }

}
