package au.csiro.data61.aap.etl.parsing;

import java.io.InputStream;
import java.util.function.Function;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.etl.MethodResult;
import au.csiro.data61.aap.etl.core.filters.Program;
import au.csiro.data61.aap.parser.XbelLexer;
import au.csiro.data61.aap.parser.XbelParser;

/**
 * Parser
 */
public class SpecificationParser {
    private static final Logger LOG = Logger.getLogger(SpecificationParser.class.getName());

    public SpecificationParserResult<Program> parseDocument(InputStream is) {
        return this.parse(is, XbelParser::document);
    } 

    protected <T> SpecificationParserResult<T> parse(InputStream is, Function<XbelParser, ParseTree> rule) {
        if (is == null) {
            LOG.severe("The 'is' parameter was null.");
            return SpecificationParserResult.ofError("The 'is' parameter was null.");
        }

        final MethodResult<CharStream> charStreamResult = SpecificationParserUtil.charStreamfromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            LOG.severe("Creation of CharStream failed.");
            return SpecificationParserResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final ErrorCollector errorCollector = new ErrorCollector();
        final XbelLexer lexer = new XbelLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorCollector);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);        
        final XbelParser syntacticParser = new XbelParser(tokens);
        syntacticParser.removeErrorListeners();
        syntacticParser.addErrorListener(errorCollector);

        final ParseTree tree = rule.apply(syntacticParser);      
        if (errorCollector.hasErrors()) {
            LOG.info("Errors during syntactic parsing.");
            return SpecificationParserResult.ofErrors(errorCollector.errorStream());
        }

        final SemanticAnalysis semanticAnalysis = new SemanticAnalysis(errorCollector);
        semanticAnalysis.analyze(tree);

        if (errorCollector.hasErrors()) {
            LOG.info("Errors during semantic analysis.");
            return SpecificationParserResult.ofErrors(errorCollector.errorStream());
        }

        // TODO: build model
        return SpecificationParserResult.ofResult(null);
    }


}