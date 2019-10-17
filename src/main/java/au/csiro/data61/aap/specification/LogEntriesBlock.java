package au.csiro.data61.aap.specification;

/**
 * LogEntriesBlock
 */
public class LogEntriesBlock extends Block {
    private final LogEntryDefinition definition;

    public LogEntriesBlock(LogEntryDefinition logEntryDefinition) {
        assert logEntryDefinition != null;
        this.definition = logEntryDefinition;
    }

    public LogEntryDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public void execute(ProgramState state) {
        throw new UnsupportedOperationException();
    }
}