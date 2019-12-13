package au.csiro.data61.aap.elf.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Token;

/**
 * LogEntry
 */
class LogEntryItem extends GeneratorItem {
    private final String eventName;
    private final List<LogEntryParameter> parameters;
    private final List<GeneratorItem> items;

    LogEntryItem(Token token, String specification, String eventName) {
        super(token, specification);
        assert eventName != null;
        this.eventName = eventName;
        this.parameters = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    String getEventName() {
        return this.eventName;
    }

    void addParameter(String type, String name, boolean indexed) {
        assert type != null;
        assert name != null;
        this.parameters.add(new LogEntryParameter(type, name, indexed));
    }

    Stream<LogEntryParameter> parameterStream() {
        return this.parameters.stream();
    }
    
    void addItem(GeneratorItem item) {
        assert item != null;
        this.items.add(item);
    }

    Stream<GeneratorItem> itemStream() {
        return this.items.stream();
    }
}