package blf;

import blf.configuration.BaseBlockchainListener;
import blf.parsing.HyperledgerSmartContractAnalyzer;
import blf.util.RootListenerException;
import blf.core.Program;
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
        final HyperledgerSmartContractAnalyzer hyperledgerSmartContractAnalyzer = new HyperledgerSmartContractAnalyzer();
        final RootListener rootListener = new RootListener(Constants.getBlockchainMap(variableExistenceListener));

        rootListener.addListener(variableExistenceListener);
        rootListener.addListener(hyperledgerSmartContractAnalyzer);

        walker.walk(rootListener, parseTree);

        BaseBlockchainListener blockchainListener = rootListener.blockchainListener;

        if (blockchainListener.containsError()) {
            throw new BcqlProcessingException("Error when configuring the data extraction.", blockchainListener.getError());
        }

        Program program = rootListener.blockchainListener.getProgram();

        program.execute(blockchainListener.getState());
    }
}
