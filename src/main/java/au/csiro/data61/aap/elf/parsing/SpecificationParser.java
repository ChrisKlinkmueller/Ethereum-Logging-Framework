package au.csiro.data61.aap.elf.parsing;

import java.io.InputStream;
import java.util.function.Function;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.util.MethodResult;
import au.csiro.data61.aap.elf.EthqlProcessingResult;

/**
 * Parser
 */
public class SpecificationParser {
    private static final Logger LOG = Logger.getLogger(SpecificationParser.class.getName());

    public EthqlProcessingResult<ParseTree> parseDocument(InputStream is) {
        return this.parse(is, EthqlParser::document);
    } 

    protected EthqlProcessingResult<ParseTree> parse(InputStream is, Function<EthqlParser, ParseTree> rule) {
        if (is == null) {
            LOG.severe("The 'is' parameter was null.");
            return EthqlProcessingResult.ofError("The 'is' parameter was null.");
        }

        final MethodResult<CharStream> charStreamResult = SpecificationParserUtil.charStreamfromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            LOG.severe("Creation of CharStream failed.");
            return EthqlProcessingResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final ErrorCollector errorCollector = new ErrorCollector();
        final EthqlLexer lexer = new EthqlLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorCollector);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);        
        final EthqlParser syntacticParser = new EthqlParser(tokens);
        syntacticParser.removeErrorListeners();
        syntacticParser.addErrorListener(errorCollector);

        final ParseTree tree = rule.apply(syntacticParser);      
        if (errorCollector.hasErrors()) {
            LOG.info("Errors during syntactic parsing.");
            return EthqlProcessingResult.ofErrors(errorCollector.errorStream());
        }

        final SemanticAnalysis semanticAnalysis = new SemanticAnalysis(errorCollector);
        semanticAnalysis.analyze(tree);

        if (errorCollector.hasErrors()) {
            LOG.info("Errors during semantic analysis.");
            return EthqlProcessingResult.ofErrors(errorCollector.errorStream());
        }

        return EthqlProcessingResult.ofResult(tree);
    }


}