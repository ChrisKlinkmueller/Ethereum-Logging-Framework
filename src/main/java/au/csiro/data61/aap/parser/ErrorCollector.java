package au.csiro.data61.aap.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * ErrorCollector
 */
class ErrorCollector extends BaseErrorListener {
    private final List<SpecificationParserError> errors;

    public ErrorCollector() {
        this.errors = new ArrayList<>();
    }

    public int errorCount() {
        return this.errors.size();
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
    
    public Stream<SpecificationParserError> errorStream() {
        return this.errors.stream();
    }

    public void addSemanticError(Token token, String errorMessage) {
        assert token != null && errorMessage != null;
        this.errors.add(new SpecificationParserError(token, errorMessage));
    }

    public void clear() {
        this.errors.clear();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        final SpecificationParserError error = new SpecificationParserError(line, charPositionInLine, msg, e);
        this.errors.add(error);
    }
}