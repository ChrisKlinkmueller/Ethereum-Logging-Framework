package au.csiro.data61.aap.elf.parsing;

import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.elf.util.RootListenerException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.util.RootListener;

/**
 * SemanticAnalysis
 */
public class SemanticAnalysis extends RootListener {

    public SemanticAnalysis(ErrorCollector errorCollector) {
        assert errorCollector != null;
        try {
            final VariableExistenceListener varAnalyzer = new VariableExistenceListener(errorCollector);
            this.addListener(new FilterNestingAnalyzer(errorCollector));
            this.addListener(new FilterDefinitionAnalyzer(errorCollector, varAnalyzer));
            this.addListener(new EmitAnalyzer(errorCollector, varAnalyzer));
            this.addListener(new ExpressionStatementAnalyzer(errorCollector, varAnalyzer));
            this.addListener(varAnalyzer);
        } catch (RootListenerException e) {
            e.printStackTrace();
        }
    }

    public SemanticAnalysis(List<SemanticAnalyzer> analyzers) {
        assert analyzers != null && analyzers.stream().allMatch(Objects::nonNull);
        analyzers.forEach(analyzer -> {
            try {
                this.addListener(analyzer);
            } catch (RootListenerException e) {
                e.printStackTrace();
            }
        });
    }

    public void analyze(ParseTree parseTree) {
        for (BcqlBaseListener listener : this.getListeners()) {
            if (listener instanceof SemanticAnalyzer) {
                ((SemanticAnalyzer) listener).clear();
            }
        }

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, parseTree);
    }

}
