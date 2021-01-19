package blf.parsing;

import java.io.InputStream;
import java.util.function.Function;

import blf.BcqllProcessingResult;
import blf.Constants;
import blf.grammar.BcqlLexer;
import blf.grammar.BcqlParser;
import blf.util.MethodResult;
import io.reactivex.annotations.NonNull;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Parser
 */
public class EthqlInterpreter {
    private final ErrorCollector errorCollector;
    private final SemanticAnalysis semanticAnalysis;

    public EthqlInterpreter(@NonNull ErrorCollector errorCollector, @NonNull SemanticAnalysis analysis) {
        this.errorCollector = errorCollector;
        this.semanticAnalysis = analysis;
    }

    public EthqlInterpreter() {
        errorCollector = new ErrorCollector();
        semanticAnalysis = new SemanticAnalysis(this.errorCollector, Constants.getBlockchainMap(new VariableExistenceListener()));
    }

    public BcqllProcessingResult<ParseTree> parseDocument(InputStream is) {
        return this.parse(is, BcqlParser::document);
    }

    protected BcqllProcessingResult<ParseTree> parse(InputStream is, Function<BcqlParser, ParseTree> rule) {
        if (is == null) {
            return BcqllProcessingResult.ofError("The 'is' parameter was null.");
        }

        final MethodResult<CharStream> charStreamResult = InterpreterUtils.charStreamfromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            return BcqllProcessingResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final BcqlLexer lexer = new BcqlLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(this.errorCollector);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final BcqlParser syntacticParser = new BcqlParser(tokens);
        syntacticParser.removeErrorListeners();
        syntacticParser.addErrorListener(this.errorCollector);

        final ParseTree tree = rule.apply(syntacticParser);
        if (errorCollector.hasErrors()) {
            return this.createErrorResultAndCleanUp();
        }

        semanticAnalysis.analyze(tree);

        if (errorCollector.hasErrors()) {
            return this.createErrorResultAndCleanUp();
        }

        return BcqllProcessingResult.ofResult(tree);
    }

    private BcqllProcessingResult<ParseTree> createErrorResultAndCleanUp() {
        final BcqllProcessingResult<ParseTree> result = BcqllProcessingResult.ofErrors(this.errorCollector.errorStream());
        this.errorCollector.clear();
        return result;
    }
}
