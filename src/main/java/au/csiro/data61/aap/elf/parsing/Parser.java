package au.csiro.data61.aap.elf.parsing;

import java.io.InputStream;
import java.util.function.Function;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.grammar.EthqlLexer;
import au.csiro.data61.aap.elf.grammar.EthqlParser;

class Parser {
    private final InterpretationEventCollector errorListener;

    Parser() {
        this.errorListener = new InterpretationEventCollector();
    }

    InterpretationResult<ParseTree> recognizeQuery(InputStream is) {
        checkNotNull(is);

        return this.recognize(is, EthqlParser::document);
    }

    InterpretationResult<ParseTree> recognize(InputStream is, Function<EthqlParser, ParseTree> parseRule) {
        final InterpretationResult<CharStream> conversionResult = ParsingUtils.charStreamfromInputStream(is);
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
}
