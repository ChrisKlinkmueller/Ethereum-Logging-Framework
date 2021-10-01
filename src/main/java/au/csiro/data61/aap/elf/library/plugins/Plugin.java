package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.parsing.InterpretationEventCollector;
import au.csiro.data61.aap.elf.parsing.SymbolTableBuilder;

public abstract class Plugin {    
    private final String name;

    protected Plugin(String name) {
        checkNotNull(name);
        checkArgument(!name.isBlank());
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public PluginAnalysisRule createAnalysisRule(InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder) {
        checkNotNull(eventCollector);
        checkNotNull(symbolTableBuilder);
        return this.newAnalysisRule(eventCollector, symbolTableBuilder);
    }

    protected abstract PluginAnalysisRule newAnalysisRule(InterpretationEventCollector eventCollector, SymbolTableBuilder symbolTableBuilder);
}
