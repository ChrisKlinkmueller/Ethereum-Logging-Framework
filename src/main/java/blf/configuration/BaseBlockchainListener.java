package blf.configuration;

import blf.core.ProgramState;
import blf.core.filters.Program;
import blf.core.instructions.SetOutputFolderInstruction;
import blf.grammar.BcqlBaseListener;
import blf.grammar.BcqlParser;
import blf.parsing.VariableExistenceListener;
import blf.util.TypeUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

/**
 * BaseBlockchainListener
 */
public abstract class BaseBlockchainListener extends BcqlBaseListener {
    private static final Logger LOGGER = Logger.getLogger(BaseBlockchainListener.class.getName());

    protected Program program;
    protected ProgramState state;
    protected BuildException error;
    protected final VariableExistenceListener variableAnalyzer;
    protected final SpecificationComposer composer = new SpecificationComposer();
    protected final Deque<Object> genericFilterPredicates = new ArrayDeque<>();

    public ProgramState getState() {
        return this.state;
    }

    public Program getProgram() {
        return this.program;
    }

    public BuildException getError() {
        return this.error;
    }

    public boolean containsError() {
        return this.error != null;
    }

    protected BaseBlockchainListener(VariableExistenceListener analyzer) {
        this.variableAnalyzer = analyzer;
    }

    @Override
    public void enterBlockchain(BcqlParser.BlockchainContext ctx) {
        LOGGER.info("Prepare program build");
        this.error = null;
        try {
            this.composer.prepareProgramBuild();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Preparation of program build failed: %s", e.getMessage()));
            System.exit(1);
        }
    }

    @Override
    public void enterOutputFolder(BcqlParser.OutputFolderContext ctx) {
        final BcqlParser.LiteralContext literal = ctx.literal();
        final String literalText = literal.getText();

        if (literal.STRING_LITERAL() == null) {
            LOGGER.severe("SET OUTPUT FOLDER parameter should be a String");
            System.exit(1);
        }

        this.state.outputFolderPath = TypeUtils.parseStringLiteral(literalText);

        this.composer.instructionListsStack.peek().add(new SetOutputFolderInstruction());
    }

    @Override
    public void exitDocument(BcqlParser.DocumentContext ctx) {
        LOGGER.info("Build program");
        try {
            this.program = this.composer.buildProgram();
        } catch (BuildException e) {
            LOGGER.severe(String.format("Building program failed: %s", e.getMessage()));
            System.exit(1);
        } finally {
            this.genericFilterPredicates.clear();
        }
    }
}
