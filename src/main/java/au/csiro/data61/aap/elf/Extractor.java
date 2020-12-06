package au.csiro.data61.aap.elf;

import au.csiro.data61.aap.elf.util.RootListenerException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.configuration.EthereumListener;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.VariableExistenceListener;
import au.csiro.data61.aap.elf.util.RootListener;

/**
 * Extractor
 */
public class Extractor {

    public void extractData(final String bcqlFilepath) throws BcqlProcessingException, RootListenerException {

        final ParseTree parseTree = Validator.createParseTree(bcqlFilepath);

        final RootListener rootListener = new RootListener();
        final VariableExistenceListener variableExistenceListener = new VariableExistenceListener();
        final EthereumListener ethereumListener = new EthereumListener(variableExistenceListener);
        final ParseTreeWalker walker = new ParseTreeWalker();

        rootListener.addListener(variableExistenceListener);
        rootListener.addListener(ethereumListener);

        walker.walk(rootListener, parseTree);

        if (ethereumListener.containsError()) {
            throw new BcqlProcessingException("Error when configuring the data extraction.", ethereumListener.getError());
        }

        final Program program = ethereumListener.getProgram();
        this.executeProgram(program);
    }

    private void executeProgram(Program program) {
        final ProgramState state = new ProgramState();
        program.execute(state);
    }
}
