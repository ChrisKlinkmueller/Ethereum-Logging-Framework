package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.grammar.EthqlParser;
import au.csiro.data61.aap.elf.grammar.EthqlParser.PluginStatementContext;
import au.csiro.data61.aap.elf.parsing.AnalysisRule;
import au.csiro.data61.aap.elf.parsing.InterpretationEvent;
import au.csiro.data61.aap.elf.parsing.InterpretationEventCollector;
import au.csiro.data61.aap.elf.parsing.SymbolTableBuilder;
import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

public abstract class PluginAnalysisRule extends AnalysisRule {
    protected enum Action {
        CONFIGURE,
        EMIT,
        EXTRACT;

        @Override
        public String toString() {
            switch (this) {
                case CONFIGURE : return "CONFIGURE";
                case EMIT : return "EMIT";
                case EXTRACT : return "EXTRACT";
                default : throw new IllegalArgumentException("Unsupported action!");
            }
        }
    }

    private final String name;

    protected PluginAnalysisRule(String name, InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        super(eventCollector, symbolTableBuilder);

        checkNotNull(name);
        checkArgument(!name.isBlank());
        this.name = name;
    }

    protected abstract boolean isActionSupported(Action action);
    protected abstract void parseQuery(Action action, Token queryToken);

    @Override
    public void enterPluginStatement(PluginStatementContext ctx) {
        if (!ctx.plugin.getText().equals(this.name)) {
            return;
        }
        
        final Action action = this.determineAction(ctx);
        if (!this.isActionSupported(action)) {
            final String message = String.format("Plugin '%s' does not support action '%s'.", this.name, action);
            this.eventCollector.addEvent(new InterpretationEvent(Type.ERROR, message));
            return;
        }

        this.parseQuery(action, ctx.query);        
    }

    private Action determineAction(PluginStatementContext ctx) {
        switch (ctx.action.getType()) {
            case EthqlParser.KEY_CONFIGURE : return Action.CONFIGURE;
            case EthqlParser.KEY_EMIT : return Action.EMIT;
            case EthqlParser.KEY_EXTRACT : return Action.EXTRACT;
            default : throw new IllegalArgumentException(String.format("Unknown action token '%s'", ctx.action.getText()));
        }
    }
}
