package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

public class InterpretationEventCollector extends BaseErrorListener {
    private final List<InterpretationEvent> events;

    InterpretationEventCollector() {
        this.events = new LinkedList<>();
    }

    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int column,
        String message,
        RecognitionException cause
    ) {
        this.events.add(new InterpretationEvent(Type.ERROR, line, column, message, cause));
    }

    void addEvent(InterpretationEvent event) {
        checkNotNull(event);
        this.events.add(event);
    }

    InterpretationResult<ParseTree> createResult(ParseTree parseTree) {
        checkNotNull(parseTree);
        return this.events.isEmpty()
            ? InterpretationResult.of(parseTree)
            : InterpretationResult.failure(this.events);
    }
}
