package au.csiro.data61.aap.etl;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.etl.configuration.EthqlProgramBuilder;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.filters.Program;
import au.csiro.data61.aap.etl.parsing.EthqlListener;
import au.csiro.data61.aap.etl.parsing.VariableAnalyzer;
import au.csiro.data61.aap.etl.util.CompositeEthqlListener;

/**
 * Extractor
 */
public class Extractor {

    public void extractData(final String ethqlFile) throws EthqlProcessingException {
        final ParseTree parseTree = this.createParseTree(ethqlFile);

        final CompositeEthqlListener<EthqlListener> rootListener = new CompositeEthqlListener<>();
        final VariableAnalyzer analyzer = new VariableAnalyzer();
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