package au.csiro.data61.aap.elf.parsing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.library.Library;

public class Interpreter {
    private final Parser parser;
    private final Analyzer analyzer;

    public Interpreter(Library library) {
        checkNotNull(library);
        this.parser = new Parser();
        this.analyzer = new Analyzer(library);
    }

    public InterpretationResult<ParseTree> interpretFile(String filepath) {
        checkNotNull(filepath);        
        return this.interpretFile(new File(filepath));
    }

    public InterpretationResult<ParseTree> interpretFile(Path filepath) {
        checkNotNull(filepath);
        return this.interpretFile(filepath.toFile());
    }

    public InterpretationResult<ParseTree> interpretFile(File file) {
        checkNotNull(file);
        checkArgument(file.exists() && file.isFile());

        try {
            final InputStream is = new FileInputStream(file);
            return this.interpretInputStream(is);
        } catch (FileNotFoundException cause) {
            final String message = String.format("Error reading file '%s'.", file.getAbsolutePath());
            Logger.getGlobal().log(Level.SEVERE, message, cause);
            return InterpretationResult.failure(message, cause);
        }
    }

    public InterpretationResult<ParseTree> interpretString(String query) {
        checkNotNull(query);
        checkArgument(!query.isBlank());
        return this.interpretInputStream(new ByteArrayInputStream(query.getBytes()));
    }

    public InterpretationResult<ParseTree> interpretInputStream(InputStream is) {
        checkNotNull(is);

        final InterpretationResult<ParseTree> parseResult = this.parser.recognizeQuery(is);
        if (parseResult.isFailure()) {
            return parseResult;
        }

        final ParseTree parseTree = parseResult.getResult();
        return this.analyzer.analyze(parseTree);
    }
}
