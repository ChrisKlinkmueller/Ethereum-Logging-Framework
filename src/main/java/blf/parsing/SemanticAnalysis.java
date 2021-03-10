package blf.parsing;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.configuration.BaseBlockchainListener;
import blf.grammar.BcqlBaseListener;
import blf.grammar.BcqlListener;
import blf.grammar.BcqlParser;
import blf.util.RootListenerException;
import blf.util.RootListener;
import io.reactivex.annotations.NonNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * SemanticAnalysis
 */
public class SemanticAnalysis extends RootListener {

    private static final Logger LOGGER = Logger.getLogger(SemanticAnalysis.class.getName());

    VariableExistenceListener varAnalyzer;

    public SemanticAnalysis(@NonNull ErrorCollector errorCollector, Map<String, BaseBlockchainListener> blockchainListeners) {
        super(blockchainListeners);
        try {
            this.varAnalyzer = new VariableExistenceListener(errorCollector);
            this.addListener(new FilterNestingAnalyzer(errorCollector));
            this.addListener(new FilterDefinitionAnalyzer(errorCollector, varAnalyzer));
            this.addListener(new EmitAnalyzer(errorCollector, varAnalyzer));
            this.addListener(new ExpressionStatementAnalyzer(errorCollector, varAnalyzer));
            this.addListener(varAnalyzer);
            this.addListener(new HyperledgerSmartContractAnalyzer(errorCollector));
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

    @Override
    public void enterBlockchain(BcqlParser.BlockchainContext ctx) {
        if (blockchainListeners == null) {
            return;
        }

        String blockchainKey = ctx.literal().STRING_LITERAL().getText().replace("\"", "").toLowerCase();

        BaseBlockchainListener targetBlockchainListener = blockchainListeners.get(blockchainKey);

        if (targetBlockchainListener == null) {
            LOGGER.log(Level.SEVERE, "No blockchain specified");
            System.exit(1);
        }

        varAnalyzer.setBlockchainVariables(targetBlockchainListener.getState().getBlockchainVariables());
        this.notifyListener(BcqlListener::enterBlockchain, ctx);
    }

}
