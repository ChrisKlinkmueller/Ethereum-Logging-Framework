package au.csiro.data61.aap.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.specification.Specification;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Parser
 */
public class SpecificationParser {
    private static final Logger LOG = Logger.getLogger(SpecificationParser.class.getName());

    public SpecificationParserResult parse(String filepath) {
        if (filepath == null) {
            return SpecificationParserResult.ofSingleError("Parameter 'filepath' is empty.");
        }

        final File file = new File(filepath);
        final Path path = file.toPath();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return SpecificationParserResult.ofSingleError("Parameter 'filepath' does not specify a valid path.");
        }

        try (FileInputStream stream = new FileInputStream(file)) {
            return parse(stream);
        }
        catch (Exception ex) {
            final String message = String.format("Error reading file '%s'", filepath);
            LOG.log(Level.SEVERE, message, ex);
            return SpecificationParserResult.ofSingleError(message, ex);
        }
    }   
    
    public SpecificationParserResult parse(InputStream is) {
        final MethodResult<CharStream> charStreamResult = this.fromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final AntlrErrorReporter errorReporter = new AntlrErrorReporter();
        final XbelLexer lexer = new XbelLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorReporter);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);        
        final XbelParser parser = new XbelParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorReporter);

        final ParseTree tree = parser.document();
        
        if (errorReporter.hasErrors()) {
            return SpecificationParserResult.ofErrorReporter(errorReporter);
        }

        final ParseTreeTransformer transformer = new ParseTreeTransformer();
        final Specification specification = transformer.visit(tree);

        final SemanticAnalyser analyser = new SemanticAnalyser();
        final SpecificationParserError[] semanticErrors = analyser.analyze(specification);
        if (semanticErrors.length == 0) {
            return new SpecificationParserResult(specification);
        }
        else {
            return new SpecificationParserResult(semanticErrors);
        }

    }

    private MethodResult<CharStream> fromInputStream(InputStream is) {
        try {
            final CharStream charStream = CharStreams.fromStream(is);
            return MethodResult.ofResult(charStream);
        }
        catch (Exception ex) {
            final String message = "Error processing the file content.";
            LOG.log(Level.SEVERE, message, ex);
            return MethodResult.ofError(message, ex);
        }
    }


}