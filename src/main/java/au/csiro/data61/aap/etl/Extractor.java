package au.csiro.data61.aap.etl;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.filters.Program;
import au.csiro.data61.aap.etl.parsing.EthqlBaseListener;
import au.csiro.data61.aap.etl.parsing.EthqlParser.StatementContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ValueCreationContext;

/**
 * Extractor
 */
public class Extractor {

    public void extractData(final String ethqlFile) throws EthqlProcessingException {
        final ParseTree parseTree = this.createParseTree(ethqlFile);

        final EthqlBuilderBridge builder = new EthqlBuilderBridge();
        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(builder, parseTree);

        final Program program = builder.buildProgram();
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

    private static class EthqlBuilderBridge extends EthqlBaseListener {
        private ProgramBuilder builder;

        @Override
        public void exitStatement(StatementContext ctx) {
            /*ValueCreationContext vcc = ctx.valueCreation();
            vcc.variableReference()

            vcc.methodCall()

            vcc.literal();*/
        }

        Program buildProgram() throws EthqlProcessingException {
            try {
                return this.builder.buildProgram();
            } catch (final BuildException ex) {
                throw new EthqlProcessingException("Error when building the extraction program.", ex);
            }
        }
    }
}