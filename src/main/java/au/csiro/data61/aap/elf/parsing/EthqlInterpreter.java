package au.csiro.data61.aap.elf.parsing;

import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Predicate;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.util.MethodResult;
import au.csiro.data61.aap.elf.EthqlProcessingEvent;
import au.csiro.data61.aap.elf.EthqlProcessingResult;
import au.csiro.data61.aap.elf.EthqlProcessingEvent.Type;

/**
 * Parser
 */
public class EthqlInterpreter {
    private final EventCollector errorCollector;
    private final SemanticAnalysis semanticAnalysis;

    public EthqlInterpreter(EventCollector errorCollector, SemanticAnalysis analysis) {
        assert errorCollector != null;
        assert analysis != null;
        this.errorCollector = errorCollector;
        this.semanticAnalysis = analysis;
    }

    public EthqlInterpreter() {
        errorCollector = new EventCollector();
        semanticAnalysis = new SemanticAnalysis(this.errorCollector);
    }

    public EthqlProcessingResult<ParseTree> parseDocument(InputStream is, boolean errorsOnly) {
        return this.parse(is, errorsOnly, EthqlParser::document);
    }

    protected EthqlProcessingResult<ParseTree> parse(InputStream is, boolean errorsOnly, Function<EthqlParser, ParseTree> rule) {
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
        if (errorCollector.hasEvents(errorsOnly)) {
            return this.createErrorResultAndCleanUp(errorsOnly);
        }

        semanticAnalysis.analyze(tree);

        if (errorCollector.hasEvents(errorsOnly)) {
            return this.createErrorResultAndCleanUp(errorsOnly);
        }

        return EthqlProcessingResult.ofResult(tree);
    }

    private EthqlProcessingResult<ParseTree> createErrorResultAndCleanUp(boolean errorsOnly) {
        final Predicate<EthqlProcessingEvent> predicate = errorsOnly ? e -> e.getType() == Type.ERROR : e -> true;

        final EthqlProcessingResult<ParseTree> result = EthqlProcessingResult.ofErrors(this.errorCollector.eventStream().filter(predicate));
        this.errorCollector.clear();
        return result;
    }
}
