package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.grammar.EthqlParser;
import au.csiro.data61.aap.elf.grammar.EthqlParser.PluginStatementContext;
import au.csiro.data61.aap.elf.parsing.AnalysisRule;
import au.csiro.data61.aap.elf.parsing.InterpretationEventCollector;
import au.csiro.data61.aap.elf.parsing.SymbolTableBuilder;

public abstract class PluginAnalysisRule extends AnalysisRule {
    protected enum Action {
        CONFIGURE,
        EMIT,
        EXTRACT
    }

    private final String name;

    protected PluginAnalysisRule(String name, InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        super(eventCollector, symbolTableBuilder);

        checkNotNull(name);
        checkArgument(!name.isBlank());
        this.name = name;
    }
    
    public PluginParseResult parseAction(PluginStatementContext ctx) {
        checkNotNull(ctx);
        final Action action = this.determineAction(ctx);
        return this.parse(action, ctx.code.getText());
    }

    private Action determineAction(PluginStatementContext ctx) {
        switch (ctx.action.getType()) {
            case EthqlParser.KEY_CONFIGURE : return Action.CONFIGURE;
            case EthqlParser.KEY_EMIT : return Action.EMIT;
            case EthqlParser.KEY_EXTRACT : return Action.EXTRACT;
            default : throw new IllegalArgumentException(String.format("Unknown action token '%s'", ctx.action.getText()));
        }
    }

    protected abstract PluginParseResult parse(Action action, String code);
}
