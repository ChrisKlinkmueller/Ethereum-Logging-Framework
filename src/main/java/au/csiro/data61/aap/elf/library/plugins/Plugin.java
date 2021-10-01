package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

    public PluginParseResult parseAction(PluginAction action, String code) {
        checkNotNull(action);
        checkNotNull(code);
        return this.parse(action, code);
    }

    protected abstract PluginParseResult parse(PluginAction action, String code);
}
