package au.csiro.data61.aap.elf;

import org.antlr.v4.runtime.tree.ParseTree;

import au.csiro.data61.aap.elf.generation.SolidityCodeGeneration;

/**
 * Generator
 */
public class Generator {

    public String generateLoggingFunctionality(String ethqlFilepath) throws EthqlProcessingException {
        final ParseTree parseTree = Validator.createParseTree(ethqlFilepath);
        final SolidityCodeGeneration generation = new SolidityCodeGeneration();
        return generation.generateLoggingFunctionality(parseTree);
    }

}