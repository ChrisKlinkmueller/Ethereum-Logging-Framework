package au.csiro.data61.aap.elf.generation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * LogEntry
 */
class LogEntry<T> {
    private final String name;
    private final Map<String, String> variables;
    private final List<T> functions;

    LogEntry(String name) {
        assert name != null;
        this.name = name;
        this.variables = new LinkedHashMap<>();
        this.functions = new ArrayList<>();
    }

    String getName() {
        return this.name;
    }

    void addVariable(String type, String name) {
        assert name != null;
        assert type != null;
        this.variables.put(name, type);
    }

    Stream<String> variableNames() {
        return this.variables.keySet().stream();
    }

    String getVariableType(String name) {
        assert name != null && this.variables.containsKey(name);
        return this.variables.get(name);
    }
}