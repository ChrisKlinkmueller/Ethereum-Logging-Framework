package au.csiro.data61.aap.elf.library.plugins.ethereum;

import au.csiro.data61.aap.elf.library.plugins.Plugin;
import au.csiro.data61.aap.elf.library.plugins.PluginAnalysisRule;
import au.csiro.data61.aap.elf.parsing.InterpretationEventCollector;
import au.csiro.data61.aap.elf.parsing.SymbolTableBuilder;

public class EthWeb3Plugin extends Plugin {
    private static final String NAME = "ETH_WEB3";

    protected EthWeb3Plugin() {
        super(NAME);
    }

    @Override
    protected PluginAnalysisRule newAnalysisRule(InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        return new EthWeb3AnalysisRule(this.getName(), eventCollector, symbolTableBuilder);
    }
    
}
