package blf;

import blf.configuration.BaseBlockchainListener;
import blf.util.RootListenerException;
import blf.core.ProgramState;
import blf.core.filters.Program;
import blf.parsing.VariableExistenceListener;
import blf.util.RootListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

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
