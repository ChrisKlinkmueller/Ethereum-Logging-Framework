package au.csiro.data61.aap.elf;

import au.csiro.data61.aap.elf.parsing.BcqlListener;
import au.csiro.data61.aap.elf.util.CompositeListenerException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.configuration.EthqlProgramComposer;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer;
import au.csiro.data61.aap.elf.util.CompositeEthqlListener;

/**
 * Extractor
 */
public class Extractor {

    public void extractData(final String ethqlFilepath) throws EthqlProcessingException, CompositeListenerException {

        final ParseTree parseTree = Validator.createParseTree(ethqlFilepath);

        final CompositeEthqlListener<BcqlListener> rootListener = new CompositeEthqlListener<>();
        final VariableExistenceAnalyzer analyzer = new VariableExistenceAnalyzer();
        rootListener.addListener(analyzer);
        final EthqlProgramComposer builder = new EthqlProgramComposer(analyzer);
        rootListener.addListener(builder);

        final ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(rootListener, parseTree);

        if (builder.containsError()) {
            throw new EthqlProcessingException("Error when configuring the data extraction.", builder.getError());
        }

        final Program program = builder.getProgram();
        this.executeProgram(program);
    }

    private void executeProgram(Program program) {
        final ProgramState state = new ProgramState();
        program.execute(state);
    }
}
