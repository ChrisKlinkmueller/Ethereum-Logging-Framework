package au.csiro.data61.aap.elf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.parsing.EthqlInterpreter;

/**
 * Validator class, serves mostly as a wrapper around {@link au.csiro.data61.aap.elf.parsing.EthqlInterpreter}.
 * EthqlInterpreter method {@link au.csiro.data61.aap.elf.parsing.EthqlInterpreter#parseDocument(InputStream)} takes
 * as a parameter an InputStream object.
 *
 * @see au.csiro.data61.aap.elf.parsing.EthqlInterpreter
 * @see #createParseTree
 *
 */
public class Validator {
    private final EthqlInterpreter interpreter;

    public Validator() {
        this.interpreter = new EthqlInterpreter();
    }

    public List<EthqlProcessingError> analyzeScript(String ethqlFile) throws BcqlProcessingException {
        final EthqlProcessingResult<ParseTree> result = this.parseScript(ethqlFile);
        assert result != null;
        return result.getErrors();
    }

    public List<EthqlProcessingError> analyzeScript(InputStream stream) throws BcqlProcessingException {
        final EthqlProcessingResult<ParseTree> result = this.parseScript(stream);
        return result.getErrors();
    }

    EthqlProcessingResult<ParseTree> parseScript(String ethqlFile) throws BcqlProcessingException {
        assert ethqlFile != null;
        final InputStream fileStream = this.createFileStream(ethqlFile);
        return this.parseScript(fileStream);
    }

    EthqlProcessingResult<ParseTree> parseScript(InputStream stream) throws BcqlProcessingException {
        assert stream != null;
        return this.interpreter.parseDocument(stream);
    }

    private InputStream createFileStream(String ethqlFile) throws BcqlProcessingException {
        if (ethqlFile == null) {
            throw new BcqlProcessingException("The filepath parameter was null.");
        }

        final File file = new File(ethqlFile);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new BcqlProcessingException(String.format("Invalid file path: '%s'.", ethqlFile), ex);
        }
    }

    public static ParseTree createParseTree(final String ethqlFile) throws BcqlProcessingException {
        final Validator validator = new Validator();
        final EthqlProcessingResult<ParseTree> validatorResult = validator.parseScript(ethqlFile);

        if (!validatorResult.isSuccessful()) {
            throw new BcqlProcessingException("The ethql script is not valid. For detailed analysis results, run validator.");
        }

        return validatorResult.getResult();
    }
}
