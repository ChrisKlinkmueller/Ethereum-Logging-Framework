package au.csiro.data61.aap.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import au.csiro.data61.aap.util.MethodResult;

/**
 * Parser
 */
public class SpecificationParser {
    private static final Logger LOG = Logger.getLogger(SpecificationParser.class.getName());

    public <T> SpecificationParserResult<T> parse(Path filepath) {
        if (filepath == null) {
            return SpecificationParserResult.ofSingleError("Parameter 'filepath' is empty.");
        }

        final File file = filepath.toFile();
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
    
    public <T> SpecificationParserResult<T> parse(InputStream is) {
        final MethodResult<CharStream> charStreamResult = SpecificationParserUtil.charStreamfromInputStream(is);
        if (!charStreamResult.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulMethodResult(charStreamResult);
        }

        final AntlrErrorReporter errorReporter = new AntlrErrorReporter();
        final XbelLexer lexer = new XbelLexer(charStreamResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorReporter);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);        
        final XbelParser syntacticParser = new XbelParser(tokens);
        syntacticParser.removeErrorListeners();
        syntacticParser.addErrorListener(errorReporter);

         /*final ParseTree tree = syntacticParser.document();        
        if (errorReporter.hasErrors()) {
            return SpecificationParserResult.ofErrorReporter(errorReporter);
        }

        final ParseTreeWalker walker = new ParseTreeWalker();
        final SemanticParser semanticParser = new SemanticParser();
        walker.walk(semanticParser, tree);
        if (!semanticParser.isSuccessful()) {
            return SpecificationParserResult.ofErrors(semanticParser.getErrors());
        }*/

        return null;
    }


}