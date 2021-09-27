package au.csiro.data61.aap.elf.parsing;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

class SyntaxErrorListener extends BaseErrorListener {
    private final List<InterpretationEvent> events;

    SyntaxErrorListener() {
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

    InterpretationResult<ParseTree> createResult(ParseTree parseTree) {
        return this.events.isEmpty()
            ? InterpretationResult.of(parseTree)
            : InterpretationResult.failure(this.events);
    }
}
