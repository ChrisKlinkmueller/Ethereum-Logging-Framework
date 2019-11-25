package au.csiro.data61.aap.etl.parsing;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import au.csiro.data61.aap.etl.util.MethodResult;

/**
 * SpecificationParserUtil
 */
class SpecificationParserUtil {
    private static final Logger LOG = Logger.getLogger(SpecificationParserUtil.class.getName());

    static MethodResult<CharStream> charStreamfromInputStream(InputStream is) {
        if (is == null) {
            return MethodResult.ofError("Parameter 'is' cannot be null.");
        }

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

    static MethodResult<CharStream> charStreamfromString(String string) {
        if (string == null) {
            return MethodResult.ofError("Parameter 'string' cannot be null.");
        }
        try {
            final InputStream is = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8.name()));
            return charStreamfromInputStream(is);
        }
        catch (UnsupportedEncodingException ex) {
            final String errorMessage = "UTF-8 encoding not supported for string to inputstream conversion.";
            LOG.log(Level.SEVERE, errorMessage, ex);
            return MethodResult.ofError(errorMessage, ex);
        }
    }

    static MethodResult<EthqlParser> createParser(InputStream is, ErrorCollector errorCollector) {
        final MethodResult<CharStream> charstreamResult = charStreamfromInputStream(is);
        return createParser(charstreamResult, errorCollector);
    }

    static MethodResult<EthqlParser> createParser(String string, ErrorCollector errorCollector) {
        final MethodResult<CharStream> charstreamResult = charStreamfromString(string);
        return createParser(charstreamResult, errorCollector);
    }

    private static MethodResult<EthqlParser> createParser(MethodResult<CharStream> charstreamResult, ErrorCollector errorCollector) {
        if (!charstreamResult.isSuccessful()) {
            return MethodResult.ofError(charstreamResult);
        }

        final CharStream charStream = charstreamResult.getResult();
        final EthqlLexer lexer = new EthqlLexer(charStream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);        
        final EthqlParser syntacticParser = new EthqlParser(tokens);

        if (charStream != null) {
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorCollector);
            syntacticParser.removeErrorListeners();
            syntacticParser.addErrorListener(errorCollector);
        }

        return MethodResult.ofResult(syntacticParser);
    }
}