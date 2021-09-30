package au.csiro.data61.aap.elf.parsing;

import au.csiro.data61.aap.elf.grammar.EthqlParser;
import au.csiro.data61.aap.elf.grammar.EthqlParser.PluginStatementContext;
import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

class ConfigureInPreambleRule extends AnalysisRule {
    private boolean isPreamble;

    ConfigureInPreambleRule(InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        super(eventCollector, symbolTableBuilder);        
        this.isPreamble = true;
    }
    
    @Override
    public void enterPluginStatement(PluginStatementContext ctx) {
        final boolean isConfiguration = ctx.action.getType() == EthqlParser.KEY_CONFIGURE;
        this.isPreamble &= isConfiguration;

        if (!this.isPreamble && isConfiguration) {
            final String msg = "Configuration statements must occur before any extraction and emission statements.";
            this.eventCollector.addEvent(new InterpretationEvent(Type.ERROR, ctx.start, msg));
        }
    }

}
