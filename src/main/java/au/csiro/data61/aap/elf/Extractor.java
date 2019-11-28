package au.csiro.data61.aap.elf;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.configuration.EthqlProgramBuilder;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.EthqlListener;
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer;
import au.csiro.data61.aap.elf.util.CompositeEthqlListener;

/**
 * Extractor
 */
public class Extractor {

    public void extractData(final String ethqlFile) throws EthqlProcessingException {
        final ParseTree parseTree = this.createParseTree(ethqlFile);

        final CompositeEthqlListener<EthqlListener> rootListener = new CompositeEthqlListener<>();
        final VariableExistenceAnalyzer analyzer = new VariableExistenceAnalyzer();
        rootListener.addListener(analyzer);
        final EthqlProgramBuilder builder = new EthqlProgramBuilder(analyzer);
        rootListener.addListener(builder);

        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(rootListener, parseTree);

        if (builder.containsError()) {
            throw new EthqlProcessingException("Error when configuring the data extraction.", builder.getError());
        }

        final Program program = builder.getProgram();
        this.executeProgram(program);
    }

    private ParseTree createParseTree(final String ethqlFile) throws EthqlProcessingException {
        final Validator validator = new Validator();
        final EthqlProcessingResult<ParseTree> validatorResult = validator.parseScript(ethqlFile);

        if (!validatorResult.isSuccessful()) {
            throw new EthqlProcessingException(
                    "The ethql script is not valid. For detailed analysis results, run validator.");
        }

        return validatorResult.getResult();
    }

    private void executeProgram(Program program) {
        final ProgramState state = new ProgramState();
        program.execute(state);
    }
}