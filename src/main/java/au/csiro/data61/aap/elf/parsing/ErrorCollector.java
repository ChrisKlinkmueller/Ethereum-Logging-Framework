package au.csiro.data61.aap.elf.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.EthqlProcessingEvent;
import au.csiro.data61.aap.elf.EthqlProcessingEvent.Type;

/**
 * ErrorCollector
 */
public class ErrorCollector extends BaseErrorListener {
    private final List<EthqlProcessingEvent> events;

    public ErrorCollector() {
        this.events = new ArrayList<>();
    }

    public int errorCount() {
        return this.events.size();
    }

    public boolean hasErrors() {
        return !this.events.isEmpty();
    }

    public Stream<EthqlProcessingEvent> errorStream() {
        return this.events.stream();
    }

    public void addSemanticError(Token token, String errorMessage) {
        assert token != null && errorMessage != null;
        this.events.add(new EthqlProcessingEvent(Type.ERROR, token, errorMessage));
    }

    public void addWarning(Token token, String warning) {
        assert token != null && warning != null && !warning.isBlank();
        this.events.add(new EthqlProcessingEvent(Type.WARNING, token, warning));
    }

    public void addInfo(Token token, String info) {
        assert token != null && info != null && !info.isBlank();
        this.events.add(new EthqlProcessingEvent(Type.ERROR, token, info));
    }

    public void clear() {
        this.events.clear();
    }

    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int charPositionInLine,
        String msg,
        RecognitionException e
    ) {
        final EthqlProcessingEvent event = new EthqlProcessingEvent(Type.ERROR, line, charPositionInLine, msg, e);
        this.events.add(event);
    }
}
