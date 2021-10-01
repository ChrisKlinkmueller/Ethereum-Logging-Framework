package au.csiro.data61.aap.elf.library.plugins.ethereum;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.library.plugins.PluginAnalysisRule;
import au.csiro.data61.aap.elf.parsing.InterpretationEventCollector;
import au.csiro.data61.aap.elf.parsing.SymbolTableBuilder;

class EthWeb3AnalysisRule extends PluginAnalysisRule {
    
    EthWeb3AnalysisRule(String name, InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        super(name, eventCollector, symbolTableBuilder);
    }

    @Override
    protected boolean isActionSupported(Action action) {
        return action != Action.EMIT;
    }

    @Override
    protected void parseQuery(Action action, Token queryToken) {
        throw new UnsupportedOperationException();
    }
    
}
