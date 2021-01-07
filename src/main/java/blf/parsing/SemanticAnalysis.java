package blf.parsing;

import java.util.List;

import blf.grammar.BcqlBaseListener;
import blf.util.RootListenerException;
import blf.util.RootListener;
import io.reactivex.annotations.NonNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * SemanticAnalysis
 */
public class SemanticAnalysis extends RootListener {

    public SemanticAnalysis(@NonNull ErrorCollector errorCollector) {
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

    public SemanticAnalysis(@NonNull List<SemanticAnalyzer> analyzers) {
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
