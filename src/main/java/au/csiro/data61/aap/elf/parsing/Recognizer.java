package au.csiro.data61.aap.elf.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.InterpretationEvent;
import au.csiro.data61.aap.elf.InterpretationResult;
import au.csiro.data61.aap.elf.InterpretationEvent.Type;
import au.csiro.data61.aap.elf.grammar.EthqlLexer;
import au.csiro.data61.aap.elf.grammar.EthqlParser;

class Recognizer {
    private final SyntaxErrorListener errorListener;
    
    Recognizer() {
        this.errorListener = new SyntaxErrorListener();
    }

    InterpretationResult<ParseTree> recognize(InputStream is, Function<EthqlParser, ParseTree> parseRule) {
        final InterpretationResult<CharStream> conversionResult = this.charStreamfromInputStream(is);
        if (conversionResult.isFailure()) {
            return conversionResult.convertFailure();
        }

        final EthqlLexer lexer = new EthqlLexer(conversionResult.getResult());
        lexer.removeErrorListeners();
        lexer.addErrorListener(this.errorListener);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final EthqlParser parser = new EthqlParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(this.errorListener);

        final ParseTree parseTree = parseRule.apply(parser);
        return this.errorListener.createResult(parseTree);
    }

    private InterpretationResult<CharStream> charStreamfromInputStream(InputStream is) {
        try {
            final CharStream charStream = CharStreams.fromStream(is);
            return InterpretationResult.of(charStream);
        }
        catch (IOException ex) {
            final String msg = "Error parsing the input stream.";
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            return InterpretationResult.failure(new InterpretationEvent(Type.ERROR, msg, ex));
        }
    }

}
