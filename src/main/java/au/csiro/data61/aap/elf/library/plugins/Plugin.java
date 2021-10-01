package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import au.csiro.data61.aap.elf.grammar.EthqlParser.PluginStatementContext;

public abstract class Plugin {
    private final String name;

    protected Plugin(String name) {
        checkNotNull(name);
        checkArgument(!name.isBlank());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PluginParseResult parseAction(PluginStatementContext ctx) {
        checkNotNull(ctx);
        return this.parse(ctx);
    }

    protected abstract PluginParseResult parse(PluginStatementContext ctx);
}
