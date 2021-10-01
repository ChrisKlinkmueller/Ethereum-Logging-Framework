package au.csiro.data61.aap.elf.library.plugins.ethereum;

import au.csiro.data61.aap.elf.library.plugins.PluginAnalysisRule;
import au.csiro.data61.aap.elf.library.plugins.PluginParseResult;
import au.csiro.data61.aap.elf.parsing.InterpretationEventCollector;
import au.csiro.data61.aap.elf.parsing.SymbolTableBuilder;

class EthWeb3AnalysisRule extends PluginAnalysisRule {
    
    EthWeb3AnalysisRule(String name, InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        super(name, eventCollector, symbolTableBuilder);
    }

    @Override
    protected PluginParseResult parse(Action action, String code) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
