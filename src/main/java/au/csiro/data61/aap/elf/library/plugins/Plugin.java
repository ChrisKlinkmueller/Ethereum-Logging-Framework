package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Plugin {    
    private final String name;
    private final PluginAnalysisRule parser;

    public Plugin(String name, PluginAnalysisRule parser) {
        checkNotNull(name);
        checkArgument(!name.isBlank());
        checkNotNull(parser);
        this.name = name;
        this.parser = parser;
    }

    public String getName() {
        return this.name;
    }

    public PluginAnalysisRule getParser() {
        return this.parser;
    }

}
