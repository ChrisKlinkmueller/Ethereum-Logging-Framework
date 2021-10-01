package au.csiro.data61.aap.elf.library.plugins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import au.csiro.data61.aap.elf.parsing.InterpretationEvent;
import au.csiro.data61.aap.elf.types.Type;
import com.google.common.base.Preconditions;

public class PluginParseResult {
    private final Type returnType;
    private final List<InterpretationEvent> events;

    public PluginParseResult(Type returnType) {
        checkNotNull(returnType);

        this.returnType = returnType;
        this.events = Collections.emptyList();
    }

    public PluginParseResult(Type returnType, InterpretationEvent... events) {
        this(returnType, List.of(events));
    }

    public PluginParseResult(Type returnType, List<InterpretationEvent> events) {
        checkNotNull(returnType);
        checkNotNull(events);
        checkArgument(!events.isEmpty());
        events.stream().forEach(Preconditions::checkNotNull);

        this.returnType = returnType;
        this.events = List.copyOf(events);
    }

    public List<InterpretationEvent> getEvents() {
        return this.events;
    }

    public Type getReturnType() {
        return this.returnType;
    }
}
