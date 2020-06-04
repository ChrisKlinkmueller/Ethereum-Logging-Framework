package au.csiro.data61.aap.elf.parsing;

import java.io.InputStream;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.util.MethodResult;
import au.csiro.data61.aap.elf.EthqlProcessingResult;

/**
 * Parser
 */
public class EthqlInterpreter {
    private final ErrorCollector errorCollector;
    private final SemanticAnalysis semanticAnalysis;

    public EthqlInterpreter(ErrorCollector errorCollector, SemanticAnalysis analysis) {
        assert errorCollector != null;
        assert analysis != null;
        this.errorCollector = errorCollector;
        this.semanticAnalysis = analysis;
    }

    public EthqlInterpreter() {
        errorCollector = new ErrorCollector();
        semanticAnalysis = new SemanticAnalysis(this.errorCollector);
    }

    public EthqlProcessingResult<ParseTree> parseDocument(InputStream is) {
        return this.parse(is, EthqlParser::document);
    }

    protected EthqlProcessingResult<ParseTree> parse(InputStream is, Function<EthqlParser, ParseTree> rule) {
        if (is == null) {
            return EthqlProcessingResult.ofError("The 'is' parameter was null.");
        }

        final MethodResult<CharStream> charStreamResult = InterpreterUtils.charStreamfromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            return EthqlProcessingResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final EthqlLexer lexer = new EthqlLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(this.errorCollector);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final EthqlParser syntacticParser = new EthqlParser(tokens);
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

        return EthqlProcessingResult.ofResult(tree);
    }

    private EthqlProcessingResult<ParseTree> createErrorResultAndCleanUp() {
        final EthqlProcessingResult<ParseTree> result = EthqlProcessingResult.ofErrors(this.errorCollector.errorStream());
        this.errorCollector.clear();
        return result;
    }
}
