package blf;

import org.antlr.v4.runtime.tree.ParseTree;

import blf.generation.SolidityCodeGeneration;

/**
 * Generator
 */
public class Generator {

    public String generateLoggingFunctionality(String ethqlFilepath) throws BcqlProcessingException {
        final ParseTree parseTree = Validator.createParseTree(ethqlFilepath);
        final SolidityCodeGeneration generation = new SolidityCodeGeneration();
        return generation.generateLoggingFunctionality(parseTree);
    }

}
