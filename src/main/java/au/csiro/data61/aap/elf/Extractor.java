package au.csiro.data61.aap.elf;

import au.csiro.data61.aap.elf.configuration.BaseBlockchainListener;
import au.csiro.data61.aap.elf.util.RootListenerException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

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

        final ParseTreeWalker walker = new ParseTreeWalker();

        final VariableExistenceListener variableExistenceListener = new VariableExistenceListener();

        final RootListener rootListener = new RootListener(Constants.getBlockchainMap(variableExistenceListener));

        rootListener.addListener(variableExistenceListener);

        walker.walk(rootListener, parseTree);

        BaseBlockchainListener blockchainListener = rootListener.blockchainListener;

        if (blockchainListener.containsError()) {
            throw new BcqlProcessingException("Error when configuring the data extraction.", blockchainListener.getError());
        }

        this.executeProgram(rootListener.blockchainListener.getProgram());
    }

    private void executeProgram(Program program) {
        final ProgramState state = new ProgramState();
        program.execute(state);
    }
}
