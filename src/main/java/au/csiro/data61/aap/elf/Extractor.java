package au.csiro.data61.aap.elf;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import au.csiro.data61.aap.elf.configuration.EthqlProgramComposer;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.EthqlListener;
import au.csiro.data61.aap.elf.parsing.VariableExistenceAnalyzer;
import au.csiro.data61.aap.elf.util.CompositeEthqlListener;

import java.io.InputStream;

/**
 * Extractor
 */
public class Extractor {
    private final ProgramState state;

    public Extractor() {
        this.state = new ProgramState();
    }

    Extractor(ProgramState state) {
        this.state = state;
    }

    public void extractData(final String ethqlFilepath) throws EthqlProcessingException {
        final ParseTree parseTree = Validator.createParseTree(ethqlFilepath);
        this.executeProgram(parseTree);
    }

    public void extractData(final InputStream stream) throws EthqlProcessingException {
        final ParseTree parseTree = Validator.createParseTree(stream);
        this.executeProgram(parseTree);
    }

    private void executeProgram(ParseTree parseTree) throws EthqlProcessingException {
        final CompositeEthqlListener<EthqlListener> rootListener = new CompositeEthqlListener<>();
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
        program.execute(this.state);
    }
}
