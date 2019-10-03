package au.csiro.data61.aap.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * ANTLRErrorHandler
 */
class AntlrErrorReporter extends BaseErrorListener {
    private final List<SpecificationParserError> errors;

    public AntlrErrorReporter() {
        this.errors = new ArrayList<>();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        System.out.println("Syntax error");
        final SpecificationParserError error = new SpecificationParserError(line, charPositionInLine, msg, e);
        this.errors.add(error);
    }
    
    public Stream<SpecificationParserError> errorStream() {
        return this.errors.stream();
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
}