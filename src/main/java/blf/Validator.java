package blf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import blf.parsing.BcqlInterpreter;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Validator class, serves mostly as a wrapper around {@link BcqlInterpreter}.
 * BcqlInterpreter method {@link BcqlInterpreter#parseDocument(InputStream)} takes
 * as a parameter an InputStream object.
 *
 * @see BcqlInterpreter
 * @see #createParseTree
 *
 */
public class Validator {
    private final BcqlInterpreter interpreter;

    public Validator() {
        this.interpreter = new BcqlInterpreter();
    }

    public List<BcqlProcessingError> analyzeScript(String bcqlFile) throws BcqlProcessingException {
        final BcqllProcessingResult<ParseTree> result = this.parseScript(bcqlFile);
        assert result != null;
        return result.getErrors();
    }

    public List<BcqlProcessingError> analyzeScript(InputStream stream) throws BcqlProcessingException {
        final BcqllProcessingResult<ParseTree> result = this.parseScript(stream);
        return result.getErrors();
    }

    BcqllProcessingResult<ParseTree> parseScript(String bcqlFile) throws BcqlProcessingException {
        assert bcqlFile != null;
        final InputStream fileStream = this.createFileStream(bcqlFile);
        return this.parseScript(fileStream);
    }

    BcqllProcessingResult<ParseTree> parseScript(InputStream stream) throws BcqlProcessingException {
        assert stream != null;
        return this.interpreter.parseDocument(stream);
    }

    private InputStream createFileStream(String bcqlFile) throws BcqlProcessingException {
        if (bcqlFile == null) {
            throw new BcqlProcessingException("The filepath parameter was null.");
        }

        final File file = new File(bcqlFile);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new BcqlProcessingException(String.format("Invalid file path: '%s'.", bcqlFile), ex);
        }
    }

    public static ParseTree createParseTree(final String bcqlFile) throws BcqlProcessingException {
        final Validator validator = new Validator();
        final BcqllProcessingResult<ParseTree> validatorResult = validator.parseScript(bcqlFile);

        if (!validatorResult.isSuccessful()) {
            throw new BcqlProcessingException("The bcql script is not valid. For detailed analysis results, run validator.");
        }

        return validatorResult.getResult();
    }
}
