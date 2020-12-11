package blf.parsing;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import blf.grammar.BcqlLexer;
import blf.grammar.BcqlParser;
import blf.util.MethodResult;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * InterpreterUtils
 */
public class InterpreterUtils {
    private static final Logger LOG = Logger.getLogger(InterpreterUtils.class.getName());

    static MethodResult<CharStream> charStreamfromInputStream(InputStream is) {
        if (is == null) {
            return MethodResult.ofError("Parameter 'is' cannot be null.");
        }

        try {
            final CharStream charStream = CharStreams.fromStream(is);
            return MethodResult.ofResult(charStream);
        } catch (Exception ex) {
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
        } catch (UnsupportedEncodingException ex) {
            final String errorMessage = "UTF-8 encoding not supported for string to inputstream conversion.";
            LOG.log(Level.SEVERE, errorMessage, ex);
            return MethodResult.ofError(errorMessage, ex);
        }
    }

    static MethodResult<BcqlParser> createParser(InputStream is, ErrorCollector errorCollector) {
        final MethodResult<CharStream> charstreamResult = charStreamfromInputStream(is);
        return createParser(charstreamResult, errorCollector);
    }

    static MethodResult<BcqlParser> createParser(String string, ErrorCollector errorCollector) {
        final MethodResult<CharStream> charstreamResult = charStreamfromString(string);
        return createParser(charstreamResult, errorCollector);
    }

    private static MethodResult<BcqlParser> createParser(MethodResult<CharStream> charstreamResult, ErrorCollector errorCollector) {
        if (!charstreamResult.isSuccessful()) {
            return MethodResult.ofError(charstreamResult);
        }

        final CharStream charStream = charstreamResult.getResult();
        final BcqlLexer lexer = new BcqlLexer(charStream);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final BcqlParser syntacticParser = new BcqlParser(tokens);

        if (charStream != null) {
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorCollector);
            syntacticParser.removeErrorListeners();
            syntacticParser.addErrorListener(errorCollector);
        }

        return MethodResult.ofResult(syntacticParser);
    }

    public static String determineType(BcqlParser.ValueExpressionContext ctx, VariableExistenceListener varAnalyzer) {
        return ctx.literal() != null ? literalType(ctx.literal()) : varAnalyzer.getVariableType(ctx.variableName().getText());
    }

    public static String literalType(BcqlParser.LiteralContext ctx) {
        if (ctx.BOOLEAN_LITERAL() != null) {
            return TypeUtils.BOOL_TYPE_KEYWORD;
        } else if (ctx.BYTES_LITERAL() != null) {
            return TypeUtils.BYTES_TYPE_KEYWORD;
        } else if (ctx.INT_LITERAL() != null) {
            return TypeUtils.INT_TYPE_KEYWORD;
        } else if (ctx.STRING_LITERAL() != null) {
            return TypeUtils.STRING_TYPE_KEYWORD;
        } else if (ctx.arrayLiteral() != null) {
            if (ctx.arrayLiteral().booleanArrayLiteral() != null) {
                return TypeUtils.toArrayType(TypeUtils.BOOL_TYPE_KEYWORD);
            }
            if (ctx.arrayLiteral().bytesArrayLiteral() != null) {
                return TypeUtils.toArrayType(TypeUtils.BYTES_TYPE_KEYWORD);
            }
            if (ctx.arrayLiteral().intArrayLiteral() != null) {
                return TypeUtils.toArrayType(TypeUtils.INT_TYPE_KEYWORD);
            }
            if (ctx.arrayLiteral().stringArrayLiteral() != null) {
                return TypeUtils.toArrayType(TypeUtils.STRING_TYPE_KEYWORD);
            }
        }

        throw new UnsupportedOperationException(String.format("Literal '%s' not supported", ctx.getText()));
    }
}
