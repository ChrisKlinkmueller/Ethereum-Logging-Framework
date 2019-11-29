package au.csiro.data61.aap.elf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

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
    
    public List<EthqlProcessingError> analyzeScript(String ethqlFile) throws EthqlProcessingException {
        final EthqlProcessingResult<ParseTree> result = parseScript(ethqlFile);
        assert result != null;
        return result.errorStream().collect(Collectors.toList());
    }
    
    EthqlProcessingResult<ParseTree> parseScript(String ethqlFile) throws EthqlProcessingException {
        final InputStream fileStream = this.createFileStream(ethqlFile);
        return this.interpreter.parseDocument(fileStream);        
    }

    private InputStream createFileStream(String ethqlFile) throws EthqlProcessingException {
        if (ethqlFile == null) {
            throw new EthqlProcessingException("The filepath parameter was null.");
        }

        final File file = new File(ethqlFile);
        try {
            return new FileInputStream(file);
        }
        catch (FileNotFoundException ex) {
            throw new EthqlProcessingException(String.format("Invalid file path: '%'.", ethqlFile), ex);
        }
    }

        
}