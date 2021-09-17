package au.csiro.data61.aap.elf.parsing;

import java.io.InputStream;

import org.antlr.v4.runtime.tree.ParseTree;

import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.InterpretationResult;
import au.csiro.data61.aap.elf.Pipeline;
import au.csiro.data61.aap.elf.grammar.EthqlParser;

public class Parser {
    private final Recognizer recognizer;
    private final Builder builder;

    public Parser() {
        this.recognizer = new Recognizer();
        this.builder = new Builder();
    }

    public InterpretationResult<Pipeline<Statement>> read(InputStream is) {
        checkNotNull(is);

        final InterpretationResult<ParseTree> recognitionResult = this.recognizer.recognize(is, EthqlParser::document);
        if (recognitionResult.isFailure()) {
            return recognitionResult.convertFailure();
        }

        return this.builder.buildPipeline(recognitionResult.getResult());
    }
}
