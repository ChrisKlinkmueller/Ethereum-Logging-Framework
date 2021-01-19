package blf;

import org.antlr.v4.runtime.tree.ParseTree;

import blf.generation.SolidityCodeGeneration;

/**
 * Generator
 */
public class Generator {

    public String generateLoggingFunctionality(String bcqlFilepath) throws BcqlProcessingException {
        final ParseTree parseTree = Validator.createParseTree(bcqlFilepath);
        final SolidityCodeGeneration generation = new SolidityCodeGeneration();
        return generation.generateLoggingFunctionality(parseTree);
    }

}
