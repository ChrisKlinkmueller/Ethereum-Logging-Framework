package au.csiro.data61.aap.elf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.parsing.EthqlInterpreter;;

/**
 * Validator
 */
public class Validator {
    private final EthqlInterpreter interpreter;

    public Validator() {
        this.interpreter = new EthqlInterpreter();
    }

    public List<EthqlProcessingError> analyzeScript(String ethqlFile)
            throws EthqlProcessingException {
        final EthqlProcessingResult<ParseTree> result = this.parseScript(ethqlFile);
        assert result != null;
        return result.getErrors();
    }

    public List<EthqlProcessingError> analyzeScript(InputStream stream)
            throws EthqlProcessingException {
        final EthqlProcessingResult<ParseTree> result = this.parseScript(stream);
        return result.getErrors();
    }

    EthqlProcessingResult<ParseTree> parseScript(String ethqlFile) throws EthqlProcessingException {
        assert ethqlFile != null;
        final InputStream fileStream = this.createFileStream(ethqlFile);
        return parseScript(fileStream);
    }

    EthqlProcessingResult<ParseTree> parseScript(InputStream stream)
            throws EthqlProcessingException {
        assert stream != null;
        return this.interpreter.parseDocument(stream);
    }

    private InputStream createFileStream(String ethqlFile) throws EthqlProcessingException {
        if (ethqlFile == null) {
            throw new EthqlProcessingException("The filepath parameter was null.");
        }

        final File file = new File(ethqlFile);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new EthqlProcessingException(String.format("Invalid file path: '%s'.", ethqlFile),
                    ex);
        }
    }

    public static ParseTree createParseTree(final String ethqlFile)
            throws EthqlProcessingException {
        final Validator validator = new Validator();
        final EthqlProcessingResult<ParseTree> validatorResult = validator.parseScript(ethqlFile);

        if (!validatorResult.isSuccessful()) {
            throw new EthqlProcessingException(
                    "The ethql script is not valid. For detailed analysis results, run validator.");
        }

        return validatorResult.getResult();
    }
}
